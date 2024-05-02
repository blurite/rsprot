package net.rsprot.protocol.api.js5

import com.github.michaelbull.logging.InlineLogger
import io.netty.buffer.ByteBuf
import net.rsprot.protocol.api.js5.Js5GroupProvider.Js5GroupType
import net.rsprot.protocol.api.js5.util.UniqueQueue
import net.rsprot.protocol.api.logging.js5Log
import net.rsprot.protocol.js5.incoming.Js5GroupRequest
import net.rsprot.protocol.js5.outgoing.Js5GroupResponse
import kotlin.math.min

public class Js5Service<T : Js5GroupType>(
    private val provider: Js5GroupProvider<T>,
) : Runnable {
    @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
    @PublishedApi
    internal val lock: Object = Object()
    private val clients = UniqueQueue<Js5Client<T>>()

    @Volatile
    private var isRunning: Boolean = true

    override fun run() {
        while (true) {
            // TODO: Logged in mechanics, give priority over logged out players
            var client: Js5Client<T>
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
                    response = client.getNextBlock(provider) ?: continue
                    flush = client.needsFlushing()
                    if (flush) {
                        client.resetByteCountTracker()
                    }
                    break
                }
            }

            serveClient(client, response, flush)
        }
    }

    private fun serveClient(
        client: Js5Client<T>,
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
            if (client.isReady()) {
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

    public fun push(
        client: Js5Client<T>,
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

    public fun readIfNotFull(client: Js5Client<T>) {
        synchronized(lock) {
            if (client.isNotFull()) {
                js5Log(logger) {
                    "Reading further JS5 requests from channel ${client.ctx.channel()}"
                }
                client.ctx.read()
            }
        }
    }

    public fun notifyIfNotEmpty(client: Js5Client<T>) {
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

    @PublishedApi
    internal inline fun use(block: () -> Unit) {
        synchronized(lock) {
            block()
        }
    }

    public fun triggerShutdown() {
        isRunning = false
        synchronized(lock) {
            lock.notifyAll()
        }
    }

    public companion object {
        public const val BLOCK_LENGTH: Int = 512
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
    }
}
