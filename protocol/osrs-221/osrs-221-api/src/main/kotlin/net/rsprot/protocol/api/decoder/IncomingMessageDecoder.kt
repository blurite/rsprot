package net.rsprot.protocol.api.decoder

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import io.netty.handler.codec.DecoderException
import net.rsprot.buffer.extensions.g1
import net.rsprot.buffer.extensions.g2
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.Prot
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.message.codec.incoming.MessageDecoderRepository

/**
 * A general-purpose incoming message decoder, responsible for decoding all types
 * of messages, including login, JS5 and game.
 * @property decoder the decoder repository containing all the decoders for each opcode
 * @property streamCipher the stream cipher used to properly decode the encrypted opcodes
 * @property state the current state in this decoder, following a typical
 * opcode -> (optional) size -> payload structure.
 * @property decoder the decoder of the current opcode
 * @property opcode the current opcode
 * @property length the current packet's real length
 */
public abstract class IncomingMessageDecoder : ByteToMessageDecoder() {
    protected abstract val decoders: MessageDecoderRepository<ClientProt>
    protected abstract val streamCipher: StreamCipher

    private enum class State {
        READ_OPCODE,
        READ_LENGTH,
        READ_PAYLOAD,
    }

    private var state: State = State.READ_OPCODE
    protected lateinit var decoder: MessageDecoder<*>
    protected var opcode: Int = -1
    protected var length: Int = 0

    override fun decode(
        ctx: ChannelHandlerContext,
        input: ByteBuf,
        out: MutableList<Any>,
    ) {
        if (state == State.READ_OPCODE) {
            if (!input.isReadable) {
                return
            }
            this.opcode = (input.g1() - streamCipher.nextInt()) and 0xFF
            this.decoder = decoders.getDecoder(opcode)
            this.length = this.decoder.prot.size
            state =
                if (this.length >= 0) {
                    State.READ_PAYLOAD
                } else {
                    State.READ_LENGTH
                }
        }

        if (state == State.READ_LENGTH) {
            when (length) {
                Prot.VAR_BYTE -> {
                    if (!input.isReadable(Byte.SIZE_BYTES)) {
                        return
                    }
                    this.length = input.g1()
                }

                Prot.VAR_SHORT -> {
                    if (!input.isReadable(Short.SIZE_BYTES)) {
                        return
                    }
                    this.length = input.g2()
                }

                else -> {
                    throw IllegalStateException("Invalid length: $length")
                }
            }
            state = State.READ_PAYLOAD
        }

        if (state == State.READ_PAYLOAD) {
            if (!input.isReadable(length)) {
                return
            }
            decodePayload(ctx, input, out)

            state = State.READ_OPCODE
        }
    }

    /**
     * Decodes the payload of this packet.
     * Open implementation as game messages have further count tracking and
     * more complex logic to deal with pausing the decoding.
     */
    protected open fun decodePayload(
        ctx: ChannelHandlerContext,
        input: ByteBuf,
        out: MutableList<Any>,
    ) {
        val payload = input.readSlice(length)
        out += decoder.decode(payload.toJagByteBuf())
        if (payload.isReadable) {
            throw DecoderException(
                "Decoder ${decoder.javaClass} did not read entire payload " +
                    "of opcode $opcode: ${payload.readableBytes()}",
            )
        }
    }
}
