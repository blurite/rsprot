package net.rsprot.protocol.game.outgoing.codec.social

import io.netty.channel.ChannelHandlerContext
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.channel.ChannelAttributes
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.game.outgoing.social.MessagePrivateEcho
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class MessagePrivateEchoEncoder : MessageEncoder<MessagePrivateEcho> {
    override val prot: ServerProt = GameServerProt.MESSAGE_PRIVATE_ECHO

    override fun encode(
        ctx: ChannelHandlerContext,
        buffer: JagByteBuf,
        message: MessagePrivateEcho,
    ) {
        val huffmanCodec =
            ctx.channel().attr(ChannelAttributes.HUFFMAN_CODEC).get()
                ?: throw IllegalStateException("Huffman codec not initialized.")
        buffer.pjstr(message.recipient)
        huffmanCodec.encode(buffer, message.message)
    }
}
