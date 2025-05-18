package net.rsprot.protocol.api.js5

import com.github.michaelbull.logging.InlineLogger
import io.netty.buffer.ByteBuf
import net.rsprot.protocol.api.js5.util.UniqueQueue
import net.rsprot.protocol.api.logging.js5Log
import net.rsprot.protocol.js5.incoming.Js5GroupRequest
import net.rsprot.protocol.js5.outgoing.Js5GroupResponse
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import kotlin.math.min

/**
 * A single-threaded JS5 service implementation used to fairly feed
 * all connected clients, with a priority on those in the logged in state.
 * @property configuration the configuration to use for writing the data to clients
 * @property provider the provider for JS5 groups to write over
 */
public class Js5Service(
    private val configuration: Js5Configuration,
    private val provider: Js5GroupProvider,
) : Runnable {
    private val clients = UniqueQueue<Js5Client>()
    private val connectedClients = ArrayDeque<Js5Client>()

    @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
    @PublishedApi
    internal val lock: Object = Object()

    @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
    private val clientLock: Object = Object()

    @Volatile
    private var isRunning: Boolean = true

    override fun run() {
        while (true) {
            try {
                var client: Js5Client
                var response: Js5GroupResponse
                var flush: Boolean
                synchronized(lock) {
                    while (true) {
                        if (!isRunning) {
                            return
                        }
                        val next = clients.removeFirstOrNull()
                        if (next == null) {
                            lock.wait()
                            continue
                        }
                        client = next
                        if (!client.ctx.channel().isActive) {
                            continue
                        }
                        val priority = client.priority
                        val ratio =
                            if (priority == Js5Client.ClientPriority.HIGH) {
                                configuration.priorityRatio
                            } else {
                                1
                            }
                        try {
                            response = client.getNextBlock(
                                configuration.missingGroupBehaviour,
                                provider,
                                configuration.blockSizeInBytes * ratio,
                            ) ?: continue
                        } catch (t: Throwable) {
                            logger.warn(t) {
                                "Unable to serve channel '${client.ctx.channel()}', dropping connection."
                            }
                            client.ctx.close()
                            continue
                        }
                        flush =
                            client.needsFlushing(
                                configuration.flushThresholdInBytes,
                                configuration.flushThresholdInRequests,
                            )
                        if (flush) {
                            client.resetTracker()
                        }
                        break
                    }
                }

                try {
                    serveClient(client, response, flush)
                } catch (t: Throwable) {
                    logger.error(t) {
                        "Unable to serve channel ${client.ctx.channel()}, dropping connection."
                    }
                    client.ctx.close()
                }
            } catch (t: Throwable) {
                logger.error(t) {
                    "Error in JS5 service processing - JS5 service has been killed."
                }
                throw t
            }
        }
    }

    private fun prefetch(): Runnable =
        Runnable {
            // Ensure the connectedClients collection doesn't modify during it, as modifications
            // during iteration may not occur
            synchronized(clientLock) {
                for (client in connectedClients) {
                    // Obtain a short-lived lock to avoid blocking the service for long periods
                    // This is to ensure we don't run into concurrency issues.
                    synchronized(lock) {
                        if (client.transferPrefetch(
                                provider,
                                configuration.prefetchTransferThresholdInBytes,
                            )
                        ) {
                            clients.add(client)
                            lock.notifyAll()
                            if (client.isNotFull()) {
                                client.ctx.read()
                            }
                        }
                    }
                }
            }
        }

    internal fun onClientConnected(client: Js5Client) {
        synchronized(clientLock) {
            this.connectedClients += client
        }
    }

    internal fun onClientDisconnected(client: Js5Client) {
        synchronized(clientLock) {
            this.connectedClients -= client
        }
    }

    /**
     * Serves a client with a jS5 response which may only be a subsection of a full group.
     * @param client the client to serve
     * @param response the response to write to the client
     * @param flush whether to flush the channel after writing this request
     */
    private fun serveClient(
        client: Js5Client,
        response: Js5GroupResponse,
        flush: Boolean,
    ) {
        val ctx = client.ctx
        ctx.write(response)
        js5Log(logger) {
            "Serving channel '${ctx.channel()}' with response: $response"
        }
        if (flush) {
            js5Log(logger) {
                "Flushing channel ${ctx.channel()}"
            }
            ctx.flush()
        }
        synchronized(lock) {
            if (!flush || client.isReady()) {
                js5Log(logger) {
                    "Continuing to serve channel ${ctx.channel()}"
                }
                clients.add(client)
            } else {
                js5Log(logger) {
                    "No longer serving channel ${ctx.channel()}"
                }
            }

            val notFull = client.isNotFull()
            if (notFull) {
                ctx.read()
            }
            js5Log(logger) {
                "Reading further JS5 requests from channel ${ctx.channel()}"
            }
        }
    }

    /**
     * Pushes a new JS5 request to this client
     * @param client the client to push the request to
     * @param request the request to push to this client
     */
    public fun push(
        client: Js5Client,
        request: Js5GroupRequest,
    ) {
        synchronized(lock) {
            client.push(request)

            if (client.isReady()) {
                clients.add(client)
                lock.notifyAll()
            }

            if (client.isNotFull()) {
                client.ctx.read()
            }
        }
    }

    /**
     * Requests a read from the given channel if it can receive more requests.
     * @param client the client to check
     */
    public fun readIfNotFull(client: Js5Client) {
        synchronized(lock) {
            if (client.isNotFull()) {
                js5Log(logger) {
                    "Reading further JS5 requests from channel ${client.ctx.channel()}"
                }
                client.ctx.read()
            }
        }
    }

    /**
     * Notifies the lock if the list of clients is not empty, resuming the JS5
     * service in the process.
     */
    public fun notifyIfNotEmpty(client: Js5Client) {
        synchronized(lock) {
            if (client.isNotEmpty()) {
                js5Log(logger) {
                    "Channel '${client.ctx.channel()}' is now writable, continuing to serve JS5 requests."
                }
                clients.add(client)
                lock.notifyAll()
            }
        }
    }

    /**
     * Executes the [block] in a synchronized manner as the rest of the JS5.
     */
    @PublishedApi
    internal inline fun use(block: () -> Unit) {
        synchronized(lock) {
            block()
        }
    }

    /**
     * Triggers a shutdown.
     */
    public fun triggerShutdown() {
        isRunning = false
        synchronized(lock) {
            lock.notifyAll()
        }
    }

    public companion object {
        /**
         * The interval at which a terminator byte is expected in the client.
         */
        internal const val BLOCK_LENGTH: Int = 512
        private val logger: InlineLogger = InlineLogger()

        /**
         * Prepares a JS5 buffer to be in a format read to be served to the clients,
         * by splitting the payload up into chunks of 512 bytes, which each have a 0xFF
         * terminator splitting them.
         * @param archive the archive id, written as a byte at the start
         * @param group the group id, written as a short at the start
         * @param input the input byte buffer from the cache, with version information
         * stripped off
         * @param output the output byte buffer into which to write the split-up JS5 buffers.
         */
        public fun prepareJs5Buffer(
            archive: Int,
            group: Int,
            input: ByteBuf,
            output: ByteBuf,
        ) {
            val readableBytes = input.readableBytes()
            output.writeByte(archive)
            output.writeShort(group)
            // Block length - 3 as we already wrote 3 bytes at the start
            val len = min(readableBytes, BLOCK_LENGTH - 3)
            output.writeBytes(input, 0, len)

            var offset = len
            while (offset < readableBytes) {
                output.writeByte(0xFF)
                // Block length - 1 as we already wrote the separator 0xFF
                val nextBlockLength = min(readableBytes - offset, BLOCK_LENGTH - 1)
                output.writeBytes(input, offset, nextBlockLength)
                offset += nextBlockLength
            }
        }

        /**
         * Ensures that the input buffer has been correctly sliced up.
         * We run this validation on the first JS5 response we receive that is at least [BLOCK_LENGTH]
         * in size. While it is possible that the buffer isn't correctly sliced up, but just happens
         * to have 0xFF at the right positions, it's very unlikely.
         * This simply offers a little extra warning for people trying to implement the JS5 system,
         * giving a clear indication that they've not called the necessary functions in order to
         * prepare the JS5 buffers ahead of time.
         * @param buffer the byte buffer to check for valid terminators.
         * @return whether the byte buffer has been correctly sliced up.
         */
        internal fun ensureCorrectlySliced(buffer: ByteBuf): Boolean {
            val cap = buffer.readableBytes()
            for (i in BLOCK_LENGTH..<cap step BLOCK_LENGTH) {
                if (buffer.getByte(i).toInt() and 0xFF != 0xFF) {
                    return false
                }
            }
            return true
        }

        public fun startPrefetching(service: Js5Service): ScheduledExecutorService {
            val executor = Executors.newSingleThreadScheduledExecutor()
            executor.scheduleWithFixedDelay(
                service.prefetch(),
                200,
                200,
                TimeUnit.MILLISECONDS,
            )
            return executor
        }
    }
}
