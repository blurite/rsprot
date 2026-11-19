package net.rsprot.protocol.common.js5.outgoing.codec

import com.github.michaelbull.logging.InlineLogger
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
        val messageBuf = message.content()
        // Perform a quick one-time validation to ensure the server is yielding the same
        // type bytebuffers that the Netty pipeline is expecting, to avoid very expensive
        // copying (either from heap to native or native to heap memory)
        if (!validatedBufferCompatibility) {
            validatedBufferCompatibility = true
            val isPipelineDirect = buffer.buffer.isDirect
            val isMessageDirect = messageBuf.isDirect
            if (isPipelineDirect != isMessageDirect) {
                logger.warn {
                    "Incompatible JS5 buffer types; " +
                        "pipeline is direct: $isPipelineDirect, message is direct: $isMessageDirect; " +
                        "Using incompatible types means there is a more expensive copying occurring each " +
                        "time a buffer is written out."
                }
            } else {
                logger.debug { "Using compatible JS5 buffer types (direct: $isPipelineDirect)" }
            }
        }
        // Only write the bytes to the `out` variable if XOR is used
        if (message.key != 0) {
            val out = buffer.buffer
            for (i in 0..<messageBuf.readableBytes()) {
                out.writeByte(messageBuf.getByte(i).toInt() xor message.key)
            }
        }
    }

    private companion object {
        @Volatile
        private var validatedBufferCompatibility: Boolean = false
        private val logger = InlineLogger()
    }
}
