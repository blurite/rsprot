package net.rsprot.protocol.message.codec

import io.netty.buffer.ByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.message.OutgoingMessage

public interface MessageEncoder<in T : OutgoingMessage> {
    public val prot: ServerProt

    public fun encode(
        buffer: ByteBuf,
        message: T,
    )
}
