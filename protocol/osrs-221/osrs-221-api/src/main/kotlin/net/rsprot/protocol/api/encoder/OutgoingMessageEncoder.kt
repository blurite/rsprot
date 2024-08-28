package net.rsprot.protocol.api.encoder

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToByteEncoder
import net.rsprot.buffer.extensions.p1
import net.rsprot.buffer.extensions.p2
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.Prot
import net.rsprot.protocol.loginprot.outgoing.LoginResponse
import net.rsprot.protocol.message.OutgoingMessage
import net.rsprot.protocol.message.codec.outgoing.MessageEncoderRepository

/**
 * A generic message encoder for all outgoing messages, including login, JS5 and game.
 * @property cipher the stream cipher used to encrypt the opcodes, and in the case of
 * some packets, the entire payload
 * @property repository the message encoder repository containing all the encoders
 */
public abstract class OutgoingMessageEncoder : MessageToByteEncoder<OutgoingMessage>(OutgoingMessage::class.java) {
    protected abstract val cipher: StreamCipher
    protected abstract val repository: MessageEncoderRepository<*>
    protected abstract val validate: Boolean

    override fun encode(
        ctx: ChannelHandlerContext,
        msg: OutgoingMessage,
        out: ByteBuf,
    ) {
        val encoder = repository.getEncoder(msg::class.java)
        val prot = encoder.prot
        val opcode = prot.opcode
        pSmart1Or2Enc(out, opcode)

        val sizeMarker =
            when (prot.size) {
                Prot.VAR_BYTE -> {
                    out.p1(0)
                    out.writerIndex() - 1
                }
                Prot.VAR_SHORT -> {
                    out.p2(0)
                    out.writerIndex() - 2
                }
                else -> -1
            }

        val payloadMarker = out.writerIndex()
        encoder.encode(
            cipher,
            out.toJagByteBuf(),
            msg,
        )

        // Update the size based on the number of bytes written, if it's a var-* packet
        if (sizeMarker != -1) {
            val writerIndex = out.writerIndex()
            var length = writerIndex - payloadMarker

            // Ok login response is a relatively special case that requires encoding the size
            // as either 3 or 4 bytes bigger than it actually is.
            // This is because it is intended to include the header of the first packet that
            // will come after the login response, so the client knows how many bytes to expect
            // right away with the login response. This _could've_ been done differently by
            // Jagex, but it isn't, resulting in this slightly awkward code.
            // If the opcode is > 127, two bytes are needed to encode the opcode,
            // otherwise a single byte is needed. In either case, 2 more bytes are needed
            // for the size of the rebuild login packet itself.
            if (msg is LoginResponse.Ok) {
                length +=
                    if (opcode > MAX_OPCODE_VALUE_FOR_SINGLE_BYTE_OPCODE) {
                        Short.SIZE_BYTES + Short.SIZE_BYTES
                    } else {
                        Byte.SIZE_BYTES + Short.SIZE_BYTES
                    }
            }
            out.writerIndex(sizeMarker)
            when (prot.size) {
                Prot.VAR_BYTE -> {
                    if (validate) {
                        check(length in 0..UByte.MAX_VALUE.toInt()) {
                            "Server prot $prot length out of bounds; expected 0..255, received $length; message: $msg"
                        }
                    }
                    out.p1(length)
                }
                Prot.VAR_SHORT -> {
                    if (validate) {
                        check(length in 0..MAX_PAYLOAD_SIZE) {
                            "Server prot $prot length out of bounds; expected 0..40_000, received $length; message: $msg"
                        }
                    }
                    out.p2(length)
                }
            }
            out.writerIndex(writerIndex)
        } else if (validate) {
            val writerIndex = out.writerIndex()
            val length = writerIndex - payloadMarker
            check(length == prot.size) {
                "Server prot $prot length mismatch; expected ${prot.size}, received $length; message: $msg"
            }
        }
    }

    /**
     * Writes a byte or short for the opcode with all the bytes
     * encrypted using the stream cipher provided.
     * The name of this function is from a leak.
     */
    private fun pSmart1Or2Enc(
        out: ByteBuf,
        opcode: Int,
    ) {
        if (opcode < 0x80) {
            out.p1(opcode + cipher.nextInt())
        } else {
            out.p1((opcode ushr 8 or 0x80) + cipher.nextInt())
            out.p1((opcode and 0xFF) + cipher.nextInt())
        }
    }

    private companion object {
        /**
         * The highest possible value for an opcode that can still be written in a single byte
         * using a 1 or 2 byte smart.
         */
        private const val MAX_OPCODE_VALUE_FOR_SINGLE_BYTE_OPCODE: Int = 127
        private const val MAX_PAYLOAD_SIZE: Int = 40_000
    }
}
