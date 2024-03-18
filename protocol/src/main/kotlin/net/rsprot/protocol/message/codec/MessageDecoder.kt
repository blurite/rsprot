package net.rsprot.protocol.message.codec

import io.netty.buffer.ByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.message.IncomingMessage

public interface MessageDecoder<out T : IncomingMessage> {
    public val prot: ClientProt

    public fun decode(buffer: ByteBuf): T
}
