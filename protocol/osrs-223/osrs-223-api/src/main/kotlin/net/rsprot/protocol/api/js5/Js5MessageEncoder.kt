package net.rsprot.protocol.api.js5

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprot.crypto.cipher.NopStreamCipher
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.api.NetworkService
import net.rsprot.protocol.api.encoder.OutgoingMessageEncoder
import net.rsprot.protocol.message.OutgoingMessage
import net.rsprot.protocol.message.codec.outgoing.MessageEncoderRepository

/**
 * A message encoder for JS5 requests.
 */
public class Js5MessageEncoder(
    public val networkService: NetworkService<*>,
) : OutgoingMessageEncoder() {
    override val cipher: StreamCipher = NopStreamCipher
    override val repository: MessageEncoderRepository<*> =
        networkService.encoderRepositories.js5MessageDecoderRepository

    override fun encode(
        ctx: ChannelHandlerContext,
        msg: OutgoingMessage,
        out: ByteBuf,
    ) {
        // Unlike all the other encoders, JS5 does not use any opcode system
        // It simply just writes the request ids followed by the payload itself.
        val encoder = repository.getEncoder(msg::class.java)
        encoder.encode(
            ctx,
            out.toJagByteBuf(),
            msg,
        )
    }
}
