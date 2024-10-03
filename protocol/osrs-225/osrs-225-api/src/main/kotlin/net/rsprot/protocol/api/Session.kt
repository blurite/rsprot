package net.rsprot.protocol.api

import com.github.michaelbull.logging.InlineLogger
import io.netty.channel.ChannelHandlerContext
import io.netty.util.ReferenceCountUtil
import io.netty.util.ReferenceCounted
import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.api.channel.inetAddress
import net.rsprot.protocol.api.game.GameDisconnectionReason
import net.rsprot.protocol.api.game.GameMessageDecoder
import net.rsprot.protocol.api.logging.networkLog
import net.rsprot.protocol.api.metrics.addDisconnectionReason
import net.rsprot.protocol.common.RSProtFlags
import net.rsprot.protocol.game.outgoing.GameServerProtCategory
import net.rsprot.protocol.game.outgoing.zone.payload.MapProjAnim
import net.rsprot.protocol.game.outgoing.zone.payload.SoundArea
import net.rsprot.protocol.loginprot.incoming.util.LoginBlock
import net.rsprot.protocol.loginprot.incoming.util.LoginClientType
import net.rsprot.protocol.message.ConsumableMessage
import net.rsprot.protocol.message.IncomingGameMessage
import net.rsprot.protocol.message.OutgoingGameMessage
import net.rsprot.protocol.message.codec.incoming.MessageConsumer
import net.rsprot.protocol.metrics.NetworkTrafficMonitor
import java.net.InetAddress
import java.util.Queue

/**
 * The session objects are used to link the player instances together with the respective
 * session instances.
 * @param R the receiver of the game message consumers, typically a player
 * @property ctx the channel handler context behind this session, public in case the server
 * needs to directly manage it
 * @property incomingMessageQueue the message queue for incoming game messages
 * @param outgoingMessageQueueProvider the provider for outgoing message queues.
 * One queue is allocated for each possible game server prot category.
 * @property counter the incoming message counter used to determine whether to stop
 * decoding packets after too many have flooded in over a single cycle.
 * @property consumers the map of incoming game message classes to the consumers of
 * said messages
 * @property loginBlock the login block that resulted in this session being constructed
 * @property incomingGameMessageConsumerExceptionHandler the exception handler responsible
 * for managing any exceptions during the game message processing
 * @property outgoingMessageQueues the array of outgoing game messages, categorized based
 * on the server prots. This is because some packets are given priority and written
 * to the client first, despite often being computed near the end of the cycle.
 * @property inetAddress the inet address behind this connection
 * @property disconnectionHook the disconnection hook to trigger if the channel happens
 * to disconnect. It should be noted that it is the server's responsibility to set
 * the hook after a successful login.
 */
@Suppress("MemberVisibilityCanBePrivate")
public class Session<R>(
    private val trafficMonitor: NetworkTrafficMonitor<*>,
    public val ctx: ChannelHandlerContext,
    private val incomingMessageQueue: Queue<IncomingGameMessage>,
    outgoingMessageQueueProvider: MessageQueueProvider<OutgoingGameMessage>,
    private val counter: GameMessageCounter,
    private val consumers: Map<Class<out IncomingGameMessage>, MessageConsumer<R, IncomingGameMessage>>,
    private val globalConsumers: List<MessageConsumer<R, IncomingGameMessage>>,
    public val loginBlock: LoginBlock<*>,
    private val incomingGameMessageConsumerExceptionHandler: IncomingGameMessageConsumerExceptionHandler<R>,
) {
    private val outgoingMessageQueues: Array<Queue<OutgoingGameMessage>> =
        Array(GameServerProtCategory.COUNT) {
            outgoingMessageQueueProvider.provide()
        }
    public val inetAddress: InetAddress = ctx.inetAddress()
    internal var disconnectionHook: Runnable? = null
        private set

    @Volatile
    private var channelStatus: ChannelStatus = ChannelStatus.OPEN

    /**
     * Queues a game message to be written to the client based on the message's defined
     * category
     * @param message the outgoing game message to queue in its respective message queue
     */
    public fun queue(message: OutgoingGameMessage) {
        queue(message, message.category)
    }

    /**
     * Queues a game message to be written to the client based on the provided message
     * category, in case one wishes to override the categories defined by the library
     * @param message the outgoing game message
     * @param category the category of the queue to put the message in
     */
    public fun queue(
        message: OutgoingGameMessage,
        category: ServerProtCategory,
    ) {
        if (message is ConsumableMessage) {
            message.consume()
        }
        if (this.channelStatus != ChannelStatus.OPEN) return
        if (RSProtFlags.filterMissingPacketsInClient) {
            if (loginBlock.clientType == LoginClientType.DESKTOP && message is SoundArea) {
                throw IllegalArgumentException(
                    "SoundArea packet may only be sent as part of " +
                        "partial enclosed as of revision 221 on Java clients. Packet: $message",
                )
            }
            if (loginBlock.clientType != LoginClientType.DESKTOP && message is MapProjAnim) {
                throw IllegalArgumentException(
                    "MapProjAnim packet may only be sent as part of " +
                        "partial enclosed as of revision 225 on C++ clients. Packet: $message",
                )
            }
        }
        val categoryId = category.id
        val queue = outgoingMessageQueues[categoryId]
        queue += message
    }

    /**
     * Iterates through all the incoming packets over the last cycle and invokes the
     * consumer with the receiver on them. If an exception is caught, it is propagated
     * forward to the respective exception handler.
     * If too many messages were received from the server during the last cycle,
     * the decoder will have stopped accepting any new packets. If that was the case
     * at the end of this function, the packet decoding will resume.
     * @param receiver the receiver on whom to invoke the message consumers,
     * typically a player instance
     * @return the number of consumers that was invoked, this is handy in case
     * one wishes to manually track the idle status serverside and potentially
     * log the player out earlier if no packets are received over a number of cycles.
     */
    public fun processIncomingPackets(receiver: R): Int {
        if (this.channelStatus != ChannelStatus.OPEN) return 0
        var count = 0
        while (true) {
            val packet = pollIncomingMessage() ?: break
            val consumer = consumers[packet::class.java]
            checkNotNull(consumer) {
                "Consumer for packet $packet does not exist."
            }
            networkLog(logger) {
                "Processing incoming game packet from channel '${ctx.channel()}': $packet"
            }
            try {
                consumer.consume(receiver, packet)
            } catch (cause: Throwable) {
                incomingGameMessageConsumerExceptionHandler.exceptionCaught(this, packet, cause)
            }
            if (globalConsumers.isNotEmpty()) {
                for (globalConsumer in globalConsumers) {
                    try {
                        globalConsumer.consume(receiver, packet)
                    } catch (cause: Throwable) {
                        incomingGameMessageConsumerExceptionHandler.exceptionCaught(this, packet, cause)
                    }
                }
            }
            count++
        }
        onPollComplete()
        return count
    }

    /**
     * Sets a disconnection hook to be triggered if the connection to this channel is lost,
     * allowing one to safely log the player out in such case.
     * @param hook the hook runnable to invoke if the connection is lost
     * @throws IllegalStateException if a hook was already registered
     */
    public fun setDisconnectionHook(hook: Runnable) {
        val currentHook = this.disconnectionHook
        if (currentHook != null) {
            throw IllegalStateException("A disconnection hook has already been registered!")
        }
        this.disconnectionHook = hook
    }

    /**
     * Requests the channel to be closed once there's nothing more to write out, and the
     * channel has been flushed.
     */
    public fun requestClose() {
        if (this.channelStatus != ChannelStatus.OPEN) {
            return
        }
        this.channelStatus = ChannelStatus.CLOSING
    }

    /**
     * Polls one incoming game message from the queue, or null if none exists.
     */
    private fun pollIncomingMessage(): IncomingGameMessage? = incomingMessageQueue.poll()

    /**
     * Resets the incoming message counter and resumes reading.
     */
    private fun onPollComplete() {
        counter.reset()
        resumeReading()
    }

    /**
     * Flushes any queued messages to the client, if any exist.
     * The flushing process takes place in the netty event loop, thus
     * the calls to this function are non-blocking and fast.
     * If not all packets were written due to writability constraints,
     * this function will further be re-triggered when channel writability
     * turns back to true, meaning this function can be called directly
     * from the netty event loop, thus the check inside it.
     */
    public fun flush() {
        if (this.channelStatus == ChannelStatus.CLOSED ||
            !ctx.channel().isActive ||
            outgoingMessageQueues.all(Queue<OutgoingGameMessage>::isEmpty)
        ) {
            return
        }
        val eventLoop = ctx.channel().eventLoop()
        if (eventLoop.inEventLoop()) {
            writeAndFlush()
        } else {
            eventLoop.execute {
                writeAndFlush()
            }
        }
    }

    /**
     * Clears all the remaining incoming and outgoing messages, releasing any buffers that were wrapped
     * in a byte buffer holder.
     * This function should be called on logout and whenever a reconnection happens, in order
     * to get rid of any messages that got written to the session, but couldn't be flushed
     * out in time before the session became inactive.
     */
    public fun clear() {
        for (queue in outgoingMessageQueues) {
            for (message in queue) {
                if (message is ReferenceCounted) {
                    if (message.refCnt() > 0) {
                        ReferenceCountUtil.safeRelease(message)
                    }
                }
            }
            queue.clear()
        }
        for (message in incomingMessageQueue) {
            if (message is ReferenceCounted) {
                if (message.refCnt() > 0) {
                    ReferenceCountUtil.safeRelease(message)
                }
            }
        }
        incomingMessageQueue.clear()
    }

    /**
     * Writes any messages that can be written based on writability to the channel.
     * The message queues are processed in ascending order, with the highest
     * priority being the first. If the writability turns false half-way through,
     * no more messages are written out - this will be resumed when channel writability
     * changes to true again.
     * At the end of this function call, the channel is flushed.
     */
    private fun writeAndFlush() {
        val channel = ctx.channel()
        queues@ for (queue in outgoingMessageQueues) {
            packets@ while (true) {
                if (!channel.isWritable) {
                    break@queues
                }
                val next = queue.poll() ?: break@packets
                networkLog(logger) {
                    "Writing outgoing game packet to channel '${ctx.channel()}': $next"
                }
                channel.write(next, channel.voidPromise())
            }
        }
        channel.flush()
        if (this.channelStatus == ChannelStatus.CLOSING) {
            this.channelStatus = ChannelStatus.CLOSED
            trafficMonitor
                .gameChannelTrafficMonitor
                .addDisconnectionReason(
                    ctx.inetAddress(),
                    GameDisconnectionReason.LOGOUT,
                )
            channel.close()
            clear()
            networkLog(logger) {
                "Flushed outgoing game packets to channel '${ctx.channel()}', closing channel."
            }
            return
        }
        networkLog(logger) {
            val leftoverPackets = outgoingMessageQueues.sumOf(Queue<OutgoingGameMessage>::size)
            if (leftoverPackets > 0) {
                "Flushing outgoing game packets to channel " +
                    "'${ctx.channel()}': $leftoverPackets leftover packets remaining"
            } else {
                "Flushing outgoing game packets to channel ${ctx.channel()}"
            }
        }
    }

    /**
     * Sets auto-read back to true and single decode back to false.
     */
    internal fun resumeReading() {
        setReadStatus(stopReading = false)
    }

    /**
     * Sets auto-read back to false and single decode back to true.
     */
    internal fun stopReading() {
        setReadStatus(stopReading = true)
    }

    /**
     * Sets the auto-read and single decode status based on the input,
     * if the channel is still open.
     * If the single decode is set to false, netty will IMMEDIATELY stop
     * decoding any more packets. Just turning auto-read to false does not
     * have the same behavior, as by default, netty will read until the ctx
     * is empty. This ensures that we only decode exactly up to the packet limit
     * and never any more beyond that.
     * If auto-read is set back to true, netty automatically calls ctx.read(), so
     * we do not need to manually call this.
     * @param stopReading whether to stop reading more packets, or resume reading
     */
    private fun setReadStatus(stopReading: Boolean) {
        val channel = ctx.channel()
        // The decoder will be null if the channel has closed
        val decoder =
            channel.pipeline()[GameMessageDecoder::class.java]
                ?: return
        decoder.isSingleDecode = stopReading
        channel.config().isAutoRead = !stopReading
    }

    /**
     * Adds an incoming message to the incoming message queue
     */
    internal fun addIncomingMessage(incomingGameMessage: IncomingGameMessage) {
        if (this.channelStatus != ChannelStatus.OPEN) return
        incomingMessageQueue += incomingGameMessage
    }

    /**
     * Increment the message counter for the provided incoming game message,
     * based on the message's category.
     */
    internal fun incrementCounter(incomingGameMessage: IncomingGameMessage) {
        if (this.channelStatus != ChannelStatus.OPEN) return
        counter.increment(incomingGameMessage.category)
    }

    /**
     * Whether any of the incoming message categories are full, meaning
     * no more packets should be decoded.
     */
    internal fun isFull(): Boolean = counter.isFull()

    private enum class ChannelStatus {
        OPEN,
        CLOSING,
        CLOSED,
    }

    private companion object {
        private val logger: InlineLogger = InlineLogger()
    }
}
