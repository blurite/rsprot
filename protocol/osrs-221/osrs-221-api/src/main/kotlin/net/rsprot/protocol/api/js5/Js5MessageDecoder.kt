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
import net.rsprot.protocol.api.decoder.DecoderState
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.message.codec.incoming.MessageDecoderRepository

/**
 * A message decoder for JS5 packets.
 */
@Suppress("DuplicatedCode")
public class Js5MessageDecoder(
    public val networkService: NetworkService<*>,
) : ByteToMessageDecoder() {
    private val decoders: MessageDecoderRepository<ClientProt> =
        networkService
            .decoderRepositories
            .js5MessageDecoderRepository

    private var state: DecoderState = DecoderState.READ_OPCODE
    private lateinit var decoder: MessageDecoder<*>
    private var opcode: Int = -1
    private var length: Int = 0

    override fun decode(
        ctx: ChannelHandlerContext,
        input: ByteBuf,
        out: MutableList<Any>,
    ) {
        if (state == DecoderState.READ_OPCODE) {
            if (!input.isReadable) {
                return
            }
            this.opcode = input.g1() and 0xFF
            this.decoder = decoders.getDecoder(opcode)
            this.length = this.decoder.prot.size
            state =
                if (this.length >= 0) {
                    DecoderState.READ_PAYLOAD
                } else {
                    DecoderState.READ_LENGTH
                }
        }

        if (state == DecoderState.READ_LENGTH) {
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
                    throw IllegalStateException("Invalid length: $length of opcode $opcode")
                }
            }
            state = DecoderState.READ_PAYLOAD
        }

        if (state == DecoderState.READ_PAYLOAD) {
            if (!input.isReadable(length)) {
                return
            }
            val payload = input.readSlice(length)
            out += decoder.decode(payload.toJagByteBuf())
            if (payload.isReadable) {
                throw DecoderException(
                    "Decoder ${decoder.javaClass} did not read entire payload " +
                        "of opcode $opcode: ${payload.readableBytes()}",
                )
            }

            state = DecoderState.READ_OPCODE
        }
    }
}
