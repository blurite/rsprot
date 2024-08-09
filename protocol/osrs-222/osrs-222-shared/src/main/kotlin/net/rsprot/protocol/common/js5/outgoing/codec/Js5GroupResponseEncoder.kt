package net.rsprot.protocol.common.js5.outgoing.codec

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.common.js5.outgoing.prot.Js5ServerProt
import net.rsprot.protocol.js5.outgoing.Js5GroupResponse
import net.rsprot.protocol.message.codec.MessageEncoder

public class Js5GroupResponseEncoder : MessageEncoder<Js5GroupResponse> {
    override val prot: ServerProt = Js5ServerProt.JS5_GROUP_RESPONSE

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: Js5GroupResponse,
    ) {
        val offset = message.offset
        val limit = message.limit
        val messageBuf = message.content()
        if (message.key != 0) {
            val out = buffer.buffer
            for (i in offset..<limit) {
                out.writeByte(messageBuf.getByte(i).toInt() xor message.key)
            }
        } else {
            buffer.buffer.writeBytes(
                messageBuf,
                offset,
                limit,
            )
        }
    }
}
