package net.rsprot.protocol.game.outgoing.codec.social

import io.netty.channel.ChannelHandlerContext
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.channel.ChannelAttributes
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.game.outgoing.social.MessagePrivate
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class MessagePrivateEncoder : MessageEncoder<MessagePrivate> {
    override val prot: ServerProt = GameServerProt.MESSAGE_PRIVATE

    override fun encode(
        ctx: ChannelHandlerContext,
        buffer: JagByteBuf,
        message: MessagePrivate,
    ) {
        val huffmanCodecProvider =
            ctx.channel().attr(ChannelAttributes.HUFFMAN_CODEC).get()
                ?: throw IllegalStateException("Huffman codec not initialized.")
        buffer.pjstr(message.sender)
        buffer.p2(message.worldId)
        buffer.p3(message.worldMessageCounter)
        buffer.p1(message.chatCrownType)
        val huffmanCodec = huffmanCodecProvider.provide()
        huffmanCodec.encode(buffer, message.message)
    }
}
