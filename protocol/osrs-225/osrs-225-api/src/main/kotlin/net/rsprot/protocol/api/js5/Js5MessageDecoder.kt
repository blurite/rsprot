package net.rsprot.protocol.api.js5

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.DecoderException
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprot.crypto.cipher.NopStreamCipher
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.api.NetworkService
import net.rsprot.protocol.api.channel.inetAddress
import net.rsprot.protocol.api.decoder.IncomingMessageDecoder
import net.rsprot.protocol.message.codec.incoming.MessageDecoderRepository

/**
 * A message decoder for JS5 packets.
 */
public class Js5MessageDecoder(
    public val networkService: NetworkService<*>,
) : IncomingMessageDecoder() {
    override val decoders: MessageDecoderRepository<ClientProt> =
        networkService
            .decoderRepositories
            .js5MessageDecoderRepository
    override val streamCipher: StreamCipher = NopStreamCipher

    @Suppress("DuplicatedCode")
    override fun decodePayload(
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
        networkService
            .trafficMonitor
            .js5ChannelTrafficMonitor
            .incrementIncomingPackets(ctx.inetAddress(), opcode, length)
    }
}
