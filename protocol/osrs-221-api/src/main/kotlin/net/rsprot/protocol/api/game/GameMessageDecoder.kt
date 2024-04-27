package net.rsprot.protocol.api.game

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
import net.rsprot.protocol.api.NetworkService
import net.rsprot.protocol.api.Session
import net.rsprot.protocol.common.platform.PlatformType
import net.rsprot.protocol.message.IncomingGameMessage
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.message.codec.incoming.MessageDecoderRepository

public class GameMessageDecoder<R>(
    public val networkService: NetworkService<R, *>,
    private val session: Session,
    private val streamCipher: StreamCipher,
    platformType: PlatformType,
) : ByteToMessageDecoder() {
    private val decoders: MessageDecoderRepository<ClientProt> =
        networkService
            .decoderRepositories
            .gameMessageDecoderRepositories[platformType]

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
                    if (this.length > SINGLE_PACKET_MAX_ACCEPTED_LENGTH) {
                        throw DecoderException(
                            "Packet $opcode has a size exceeding limitations: ${this.length}",
                        )
                    }
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
            val messageClass = decoders.getMessageClass(this.decoder.javaClass)
            val consumer = networkService.gameMessageConsumerRepository.consumers[messageClass]
            if (consumer == null) {
                input.skipBytes(length)
            } else {
                val payload = input.readSlice(length)
                val message = decoder.decode(payload.toJagByteBuf(), networkService.messageDecodingTools)
                if (payload.isReadable) {
                    throw DecoderException(
                        "Decoder ${decoder.javaClass} did not read entire payload: ${payload.readableBytes()}",
                    )
                }
                out += message
                session.incrementCounter(message as IncomingGameMessage)
                if (session.isFull()) {
                    session.stopReading()
                }
            }

            state = State.READ_OPCODE
        }
    }

    @Suppress("unused")
    private companion object {
        /**
         * The maximum size that a single packet can have in the client.
         */
        private const val SINGLE_PACKET_MAX_PAYLOAD_LENGTH: Int = 5_000

        /**
         * The maximum size a single packet can have that the server still accepts in OldSchool.
         */
        private const val SINGLE_PACKET_MAX_ACCEPTED_LENGTH: Int = 1_600
    }
}
