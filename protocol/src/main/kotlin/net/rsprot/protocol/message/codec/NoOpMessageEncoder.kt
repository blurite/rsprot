package net.rsprot.protocol.message.codec

import io.netty.channel.ChannelHandlerContext
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.message.OutgoingMessage

/**
 * A no-operation implementation of a message encoder.
 * This will not modify the buffer in any way.
 */
public interface NoOpMessageEncoder<in T : OutgoingMessage> : MessageEncoder<T> {
    override fun encode(
        ctx: ChannelHandlerContext,
        buffer: JagByteBuf,
        message: T,
    ) {
        // No-op
    }
}
