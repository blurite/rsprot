package net.rsprot.protocol.game.outgoing.codec.friendchat

import io.netty.channel.ChannelHandlerContext
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.channel.ChannelAttributes
import net.rsprot.protocol.game.outgoing.friendchat.MessageFriendChannel
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class MessageFriendChannelEncoder : MessageEncoder<MessageFriendChannel> {
    override val prot: ServerProt = GameServerProt.MESSAGE_FRIENDCHANNEL

    override fun encode(
        ctx: ChannelHandlerContext,
        buffer: JagByteBuf,
        message: MessageFriendChannel,
    ) {
        val huffmanCodecProvider =
            ctx.channel().attr(ChannelAttributes.HUFFMAN_CODEC).get()
                ?: throw IllegalStateException("Huffman codec not initialized.")
        buffer.pjstr(message.sender)
        buffer.p8(message.channelNameBase37)
        buffer.p2(message.worldId)
        buffer.p3(message.worldMessageCounter)
        buffer.p1(message.chatCrownType)
        val huffman = huffmanCodecProvider.provide()
        huffman.encode(buffer, message.message)
    }
}
