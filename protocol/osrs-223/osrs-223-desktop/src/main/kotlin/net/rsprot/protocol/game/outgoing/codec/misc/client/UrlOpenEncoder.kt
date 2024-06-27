package net.rsprot.protocol.game.outgoing.codec.misc.client

import io.netty.channel.ChannelHandlerContext
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.channel.ChannelAttributes
import net.rsprot.protocol.game.outgoing.misc.client.UrlOpen
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class UrlOpenEncoder : MessageEncoder<UrlOpen> {
    override val prot: ServerProt = GameServerProt.URL_OPEN

    override fun encode(
        ctx: ChannelHandlerContext,
        buffer: JagByteBuf,
        message: UrlOpen,
    ) {
        val cipherPair =
            ctx.channel().attr(ChannelAttributes.STREAM_CIPHER_PAIR).get()
                ?: throw IllegalStateException("Stream cipher not initialized.")
        val encoderCipher = cipherPair.encoderCipher
        val marker = buffer.writerIndex()
        buffer.pjstr(message.url)

        // Encrypt the entire buffer with a stream cipher
        for (i in marker..<buffer.writerIndex()) {
            buffer.buffer.setByte(i, buffer.buffer.getByte(i) + encoderCipher.nextInt())
        }
    }
}
