package net.rsprot.protocol.message.codec

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.message.IncomingMessage
import net.rsprot.protocol.tools.MessageDecodingTools

public interface MessageDecoder<out T : IncomingMessage> {
    public val prot: ClientProt

    public fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): T
}
