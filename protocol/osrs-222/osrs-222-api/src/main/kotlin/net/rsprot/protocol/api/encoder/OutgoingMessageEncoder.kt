package net.rsprot.protocol.api.encoder

import com.github.michaelbull.logging.InlineLogger
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
        val startMarker = out.writerIndex()
        val encoder = repository.getEncoder(msg::class.java)
        val prot = encoder.prot
        val opcode = prot.opcode
        if (encoder.encryptedPayload) {
            pSmart1Or2Enc(out, opcode)
        } else {
            // Write a temporary value for the opcode first
            // We cannot immediately write the real stream-cipher modified opcode
            // as that alters the stream cipher's own state
            if (opcode < 0x80) {
                out.p1(0)
            } else {
                out.p2(0)
            }
        }

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

        val endMarker = out.writerIndex()
        // Update the size based on the number of bytes written, if it's a var-* packet
        if (sizeMarker != -1) {
            var length = endMarker - payloadMarker

            // LoginResponse.Ok seems to require that one includes the size of its header
            // as part of the written size. It's an odd inconsistency that's been around
            // forever.
            if (msg is LoginResponse.Ok) {
                length += Byte.SIZE_BYTES + Short.SIZE_BYTES
            }
            out.writerIndex(sizeMarker)
            when (prot.size) {
                Prot.VAR_BYTE -> {
                    if (validate) {
                        if (length !in 0..MAX_UBYTE_PAYLOAD_SIZE) {
                            out.writerIndex(startMarker)
                            logger.warn {
                                "Server prot $prot length out of bounds; " +
                                    "expected 0..255, received $length; message: $msg"
                            }
                            return
                        }
                    }
                    out.p1(length)
                }
                Prot.VAR_SHORT -> {
                    if (validate) {
                        if (length !in 0..MAX_USHORT_PAYLOAD_SIZE) {
                            out.writerIndex(startMarker)
                            logger.warn {
                                "Server prot $prot length out of bounds; " +
                                    "expected 0..40_000, received $length; message: $msg"
                            }
                            return
                        }
                    }
                    out.p2(length)
                }
            }
            out.writerIndex(endMarker)
        } else if (validate) {
            val length = endMarker - payloadMarker
            if (length != prot.size) {
                out.writerIndex(startMarker)
                logger.warn {
                    "Server prot $prot length out of bounds; " +
                        "expected 0..40_000, received $length; message: $msg"
                }
                return
            }
        }
        if (!encoder.encryptedPayload) {
            out.writerIndex(startMarker)
            pSmart1Or2Enc(out, opcode)
            out.writerIndex(endMarker)
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
        private const val MAX_USHORT_PAYLOAD_SIZE: Int = 40_000
        private const val MAX_UBYTE_PAYLOAD_SIZE: Int = 255
        private val logger = InlineLogger()
    }
}
