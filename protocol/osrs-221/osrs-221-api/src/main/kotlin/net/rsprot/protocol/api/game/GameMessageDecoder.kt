package net.rsprot.protocol.api.game

import com.github.michaelbull.logging.InlineLogger
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.DecoderException
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.api.NetworkService
import net.rsprot.protocol.api.Session
import net.rsprot.protocol.api.decoder.IncomingMessageDecoder
import net.rsprot.protocol.api.logging.networkLog
import net.rsprot.protocol.common.client.OldSchoolClientType
import net.rsprot.protocol.message.IncomingGameMessage
import net.rsprot.protocol.message.codec.incoming.MessageDecoderRepository

/**
 * A decoder for game messages, one that respects the limitations set in place
 * for incoming game messages to stop decoding after a specific threshold.
 * Furthermore, this will discard any payload of a packet if no consumer
 * has been registered, avoiding the creation of further garbage in the form
 * of decoded messages or buffer slices.
 */
public class GameMessageDecoder<R>(
    public val networkService: NetworkService<R, *>,
    private val session: Session<R>,
    override val streamCipher: StreamCipher,
    oldSchoolClientType: OldSchoolClientType,
) : IncomingMessageDecoder() {
    override val decoders: MessageDecoderRepository<ClientProt> =
        networkService
            .decoderRepositories
            .gameMessageDecoderRepositories[oldSchoolClientType]

    override fun decodePayload(
        ctx: ChannelHandlerContext,
        input: ByteBuf,
        out: MutableList<Any>,
    ) {
        if (length > SINGLE_PACKET_MAX_ACCEPTED_LENGTH) {
            throw DecoderException(
                "Opcode $opcode exceeds the natural maximum allowed length in OldSchool: " +
                    "$length > $SINGLE_PACKET_MAX_ACCEPTED_LENGTH",
            )
        }
        val messageClass = decoders.getMessageClass(this.decoder.javaClass)
        val consumerRepository = networkService.gameMessageConsumerRepositoryProvider.provide()
        val consumer = consumerRepository.consumers[messageClass]
        if (consumer == null) {
            networkLog(logger) {
                "Discarding incoming game packet from channel '${ctx.channel()}': ${messageClass.simpleName}"
            }
            input.skipBytes(length)
            return
        }
        val payload = input.readSlice(length)
        val message = decoder.decode(payload.toJagByteBuf())
        if (payload.isReadable) {
            throw DecoderException(
                "Decoder ${decoder.javaClass} did not read entire payload: ${payload.readableBytes()}",
            )
        }
        out += message
        session.incrementCounter(message as IncomingGameMessage)
        if (session.isFull()) {
            networkLog(logger) {
                "Incoming packet limit reached, no longer reading incoming game packets from channel ${ctx.channel()}"
            }
            session.stopReading()
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
        private val logger: InlineLogger = InlineLogger()
    }
}
