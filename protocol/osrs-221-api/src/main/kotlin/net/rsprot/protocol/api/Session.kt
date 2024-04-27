package net.rsprot.protocol.api

import io.netty.channel.ChannelHandlerContext
import net.rsprot.protocol.api.channel.inetAddress
import net.rsprot.protocol.api.game.GameMessageDecoder
import net.rsprot.protocol.message.IncomingGameMessage
import net.rsprot.protocol.message.OutgoingGameMessage
import java.net.InetAddress
import java.util.Queue

@Suppress("MemberVisibilityCanBePrivate")
public class Session(
    public val ctx: ChannelHandlerContext,
    private val incomingMessageQueue: Queue<IncomingGameMessage>,
    private val outgoingMessageQueue: Queue<OutgoingGameMessage>,
    private val counter: GameMessageCounter,
) {
    public val inetAddress: InetAddress = ctx.inetAddress()

    public fun queue(message: OutgoingGameMessage) {
        outgoingMessageQueue += message
    }

    public fun pollIncomingMessage(): IncomingGameMessage? {
        return incomingMessageQueue.poll()
    }

    public fun onPollComplete() {
        resumeReading()
    }

    public fun flush() {
        if (outgoingMessageQueue.isEmpty() || !ctx.channel().isActive) {
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
        while (channel.isWritable) {
            val next = outgoingMessageQueue.poll() ?: break
            channel.write(next, channel.voidPromise())
        }
        channel.flush()
    }

    internal fun resumeReading() {
        setReadStatus(stopReading = false)
    }

    internal fun stopReading() {
        setReadStatus(stopReading = true)
    }

    private fun setReadStatus(stopReading: Boolean) {
        val channel = ctx.channel()
        val decoder = channel.pipeline()[GameMessageDecoder::class.java]
        checkNotNull(decoder)
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
}
