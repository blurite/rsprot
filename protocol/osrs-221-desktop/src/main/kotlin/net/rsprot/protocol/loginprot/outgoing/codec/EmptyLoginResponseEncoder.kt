package net.rsprot.protocol.loginprot.outgoing.codec

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.message.OutgoingMessage
import net.rsprot.protocol.message.codec.MessageEncoder

public class EmptyLoginResponseEncoder<in T : OutgoingMessage>(
    override val prot: ServerProt,
) : MessageEncoder<T> {
    override fun encode(
        buffer: JagByteBuf,
        message: T,
    ) {
    }
}
