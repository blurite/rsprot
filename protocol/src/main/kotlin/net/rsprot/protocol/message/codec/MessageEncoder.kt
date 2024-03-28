package net.rsprot.protocol.message.codec

import io.netty.buffer.ByteBufAllocator
import io.netty.channel.ChannelHandlerContext
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.Prot
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.message.OutgoingMessage

public interface MessageEncoder<in T : OutgoingMessage> {
    public val prot: ServerProt

    public fun encode(
        ctx: ChannelHandlerContext,
        buffer: JagByteBuf,
        message: T,
    ): JagByteBuf

    public fun allocBuffer(allocator: ByteBufAllocator): JagByteBuf {
        return JagByteBuf(allocator.buffer(initialCapacity(), maxCapacity()))
    }

    public fun initialCapacity(): Int {
        return when (val size = prot.size) {
            Prot.VAR_BYTE -> 64
            Prot.VAR_SHORT -> 256
            else -> size
        }
    }

    public fun maxCapacity(): Int {
        return when (val size = prot.size) {
            Prot.VAR_BYTE -> 255
            Prot.VAR_SHORT -> 40_000
            else -> size
        }
    }
}
