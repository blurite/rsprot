package net.rsprot.protocol.api

import com.github.michaelbull.logging.InlineLogger
import io.netty.channel.ChannelHandlerContext
import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.api.channel.inetAddress
import net.rsprot.protocol.api.game.GameMessageDecoder
import net.rsprot.protocol.api.logging.networkLog
import net.rsprot.protocol.game.incoming.GameServerProtCategory
import net.rsprot.protocol.loginprot.incoming.util.LoginBlock
import net.rsprot.protocol.message.IncomingGameMessage
import net.rsprot.protocol.message.OutgoingGameMessage
import java.net.InetAddress
import java.util.Queue
import java.util.function.BiConsumer

@Suppress("MemberVisibilityCanBePrivate")
public class Session<R>(
    public val ctx: ChannelHandlerContext,
    private val incomingMessageQueue: Queue<IncomingGameMessage>,
    private val outgoingMessageQueueProvider: MessageQueueProvider<OutgoingGameMessage>,
    private val counter: GameMessageCounter,
    private val consumers: Map<Class<out IncomingGameMessage>, BiConsumer<R, in IncomingGameMessage>>,
    public val loginBlock: LoginBlock<*>,
) {
    private val outgoingMessageQueues: Array<Queue<OutgoingGameMessage>> =
        Array(GameServerProtCategory.COUNT) {
            outgoingMessageQueueProvider.provide()
        }
    public val inetAddress: InetAddress = ctx.inetAddress()
    internal var disconnectionHook: Runnable? = null
        private set

    public fun queue(message: OutgoingGameMessage) {
        queue(message, message.category)
    }

    public fun queue(
        message: OutgoingGameMessage,
        category: ServerProtCategory,
    ) {
        val categoryId = category.id
        val queue = outgoingMessageQueues[categoryId]
        queue += message
    }

    public fun processIncomingPackets(receiver: R): Int {
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
            consumer.accept(receiver, packet)
            count++
        }
        onPollComplete()
        return count
    }

    public fun setDisconnectionHook(hook: Runnable) {
        val currentHook = this.disconnectionHook
        if (currentHook != null) {
            throw IllegalStateException("A disconnection hook has already been registered!")
        }
        this.disconnectionHook = hook
    }

    private fun pollIncomingMessage(): IncomingGameMessage? {
        return incomingMessageQueue.poll()
    }

    private fun onPollComplete() {
        counter.reset()
        resumeReading()
    }

    public fun flush() {
        if (!ctx.channel().isActive ||
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

    internal fun resumeReading() {
        setReadStatus(stopReading = false)
    }

    internal fun stopReading() {
        setReadStatus(stopReading = true)
    }

    private fun setReadStatus(stopReading: Boolean) {
        val channel = ctx.channel()
        // The decoder will be null if the channel has closed
        val decoder =
            channel.pipeline()[GameMessageDecoder::class.java]
                ?: return
        decoder.isSingleDecode = stopReading
        channel.config().isAutoRead = !stopReading
    }

    internal fun addIncomingMessage(incomingGameMessage: IncomingGameMessage) {
        incomingMessageQueue += incomingGameMessage
    }

    internal fun incrementCounter(incomingGameMessage: IncomingGameMessage) {
        counter.increment(incomingGameMessage.category)
    }

    internal fun isFull(): Boolean {
        return counter.isFull()
    }

    private companion object {
        private val logger: InlineLogger = InlineLogger()
    }
}
