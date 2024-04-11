package net.rsprot.protocol.game.outgoing.codec.clan

import io.netty.channel.ChannelHandlerContext
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.channel.ChannelAttributes
import net.rsprot.protocol.game.outgoing.clan.MessageClanChannel
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class MessageClanChannelEncoder : MessageEncoder<MessageClanChannel> {
    override val prot: ServerProt = GameServerProt.MESSAGE_CLANCHANNEL

    override fun encode(
        ctx: ChannelHandlerContext,
        buffer: JagByteBuf,
        message: MessageClanChannel,
    ) {
        val huffmanCodec =
            ctx.channel().attr(ChannelAttributes.HUFFMAN_CODEC).get()
                ?: throw IllegalStateException("Huffman codec not initialized.")
        buffer.p1(message.clanType)
        buffer.pjstr(message.name)
        buffer.p2(message.worldId)
        buffer.p3(message.worldMessageCounter)
        buffer.p1(message.chatCrownType)
        huffmanCodec.encode(buffer, message.message)
    }
}
