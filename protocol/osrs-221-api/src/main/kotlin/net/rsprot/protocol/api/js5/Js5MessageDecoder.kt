package net.rsprot.protocol.api.js5

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import io.netty.handler.codec.DecoderException
import net.rsprot.buffer.extensions.g1
import net.rsprot.buffer.extensions.g2
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.Prot
import net.rsprot.protocol.api.NetworkService
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.message.codec.incoming.MessageDecoderRepository

public class Js5MessageDecoder(
    public val networkService: NetworkService<*, *>,
) : ByteToMessageDecoder() {
    private val decoders: MessageDecoderRepository<ClientProt> =
        networkService
            .decoderRepositories
            .js5MessageDecoderRepository

    private enum class State {
        READ_OPCODE,
        READ_LENGTH,
        READ_PAYLOAD,
    }

    private var state: State = State.READ_OPCODE
    private lateinit var decoder: MessageDecoder<*>
    private var opcode: Int = -1
    private var length: Int = 0

    override fun decode(
        ctx: ChannelHandlerContext,
        input: ByteBuf,
        out: MutableList<Any>,
    ) {
        if (state == State.READ_OPCODE) {
            if (!input.isReadable) {
                return
            }
            this.opcode = input.g1()
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
            val payload = input.readSlice(length)
            out += decoder.decode(payload.toJagByteBuf(), networkService.messageDecodingTools)
            if (payload.isReadable) {
                throw DecoderException(
                    "Decoder ${decoder.javaClass} did not read entire payload: ${payload.readableBytes()}",
                )
            }

            state = State.READ_OPCODE
        }
    }
}
