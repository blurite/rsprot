package net.rsprot.protocol.api.game

import com.github.michaelbull.logging.InlineLogger
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
import net.rsprot.protocol.api.channel.inetAddress
import net.rsprot.protocol.api.decoder.DecoderState
import net.rsprot.protocol.api.logging.networkLog
import net.rsprot.protocol.common.client.OldSchoolClientType
import net.rsprot.protocol.internal.RSProtFlags
import net.rsprot.protocol.message.IncomingGameMessage
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.message.codec.incoming.MessageDecoderRepository

/**
 * A decoder for game messages, one that respects the limitations set in place
 * for incoming game messages to stop decoding after a specific threshold.
 * Furthermore, this will discard any payload of a packet if no consumer
 * has been registered, avoiding the creation of further garbage in the form
 * of decoded messages or buffer slices.
 */
@Suppress("DuplicatedCode")
public class GameMessageDecoder<R>(
    public val networkService: NetworkService<R>,
    private val session: Session<R>,
    private val streamCipher: StreamCipher,
    oldSchoolClientType: OldSchoolClientType,
) : ByteToMessageDecoder() {
    private val decoders: MessageDecoderRepository<ClientProt> =
        networkService
            .decoderRepositories
            .gameMessageDecoderRepositories[oldSchoolClientType]

    private var state: DecoderState = DecoderState.READ_OPCODE
    private lateinit var decoder: MessageDecoder<*>
    private var opcode: Int = -1
    private var length: Int = 0

    private val previousPackets: IntArray =
        IntArray(networkService.configuration.incomingGamePacketBacklog) {
            -1
        }
    private var previousPacketIndex: Int = 0

    private fun invalidOpcodeException(): Nothing =
        throw IllegalStateException("Invalid opcode received! Previous packets: ${buildPreviousPacketLog()}")

    private fun buildPreviousPacketLog(): String =
        buildString {
            val previousPackets = this@GameMessageDecoder.previousPackets
            val previousPacketIndex = (this@GameMessageDecoder.previousPacketIndex - 1) % previousPackets.size
            for (i in previousPacketIndex downTo 0) {
                append(previousPackets[i]).append(", ")
            }
            for (i in previousPackets.size - 1 downTo (previousPacketIndex + 1)) {
                append(previousPackets[i]).append(", ")
            }
            delete(length - 2, length)
        }

    private fun mapOpcode(opcode: Int): Int {
        val mapper = networkService.clientToServerOpcodeMapper ?: return opcode
        return mapper.decode(opcode)
    }

    override fun decode(
        ctx: ChannelHandlerContext,
        input: ByteBuf,
        out: MutableList<Any>,
    ) {
        if (state == DecoderState.READ_OPCODE) {
            if (!input.isReadable) {
                return
            }
            this.opcode = mapOpcode((input.g1() - streamCipher.nextInt()) and 0xFF)
            this.previousPackets[this.previousPacketIndex++ % this.previousPackets.size] = this.opcode
            val decoderOrNull = decoders.getDecoderOrNull(opcode)
            if (decoderOrNull == null) {
                invalidOpcodeException()
            }
            this.decoder = decoderOrNull
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
                    throw IllegalStateException(
                        "Invalid length: $length of opcode $opcode, " +
                            "previous packets: ${buildPreviousPacketLog()}",
                    )
                }
            }
            state = DecoderState.READ_PAYLOAD
        }

        if (state == DecoderState.READ_PAYLOAD) {
            if (!input.isReadable(length)) {
                return
            }
            if (length > RSProtFlags.singleVarShortPacketMaxAcceptedLength) {
                throw DecoderException(
                    "Opcode $opcode exceeds the natural maximum allowed length in OldSchool: " +
                        "$length > ${RSProtFlags.singleVarShortPacketMaxAcceptedLength}, " +
                        "previous packets: ${buildPreviousPacketLog()}",
                )
            }
            networkService
                .trafficMonitor
                .gameChannelTrafficMonitor
                .incrementIncomingPackets(ctx.inetAddress(), opcode, length)
            val messageClass = decoders.getMessageClass(this.decoder.javaClass)
            val consumerRepository = networkService.gameMessageConsumerRepositoryProvider.provide()
            val consumer = consumerRepository.consumers[messageClass]
            if (consumer == null) {
                networkLog(logger) {
                    "Discarding incoming game packet from channel '${ctx.channel()}': ${messageClass.simpleName}"
                }
                input.skipBytes(length)
                state = DecoderState.READ_OPCODE
                return
            }
            val payload = input.readSlice(length)
            val message = decoder.decode(payload.toJagByteBuf())
            if (payload.isReadable) {
                throw DecoderException(
                    "Decoder ${decoder.javaClass} did not read entire payload: ${payload.readableBytes()}, " +
                        "previous packets: ${buildPreviousPacketLog()}",
                )
            }
            out += message
            session.incrementCounter(message as IncomingGameMessage)
            if (session.isFull()) {
                networkLog(logger) {
                    "Incoming packet limit reached, no longer reading " +
                        "incoming game packets from channel ${ctx.channel()}"
                }
                session.stopReading()
            }

            state = DecoderState.READ_OPCODE
        }
    }

    @Suppress("unused")
    private companion object {
        /**
         * The maximum size that a single packet can have in the client.
         */
        private const val SINGLE_PACKET_MAX_PAYLOAD_LENGTH: Int = 5_000

        private val logger: InlineLogger = InlineLogger()
    }
}
