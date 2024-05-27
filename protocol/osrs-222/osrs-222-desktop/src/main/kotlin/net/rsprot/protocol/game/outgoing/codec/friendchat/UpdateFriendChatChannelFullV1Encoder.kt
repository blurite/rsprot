package net.rsprot.protocol.game.outgoing.codec.friendchat

import io.netty.channel.ChannelHandlerContext
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.friendchat.UpdateFriendChatChannelFullV1
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class UpdateFriendChatChannelFullV1Encoder : MessageEncoder<UpdateFriendChatChannelFullV1> {
    override val prot: ServerProt = GameServerProt.UPDATE_FRIENDCHAT_CHANNEL_FULL_V1

    override fun encode(
        ctx: ChannelHandlerContext,
        buffer: JagByteBuf,
        message: UpdateFriendChatChannelFullV1,
    ) {
        buffer.pjstr(message.channelOwner)
        buffer.p8(message.channelNameBase37)
        buffer.p1(message.kickRank)
        buffer.p1(message.entries.size)
        for (entry in message.entries) {
            buffer.pjstr(entry.name)
            buffer.p2(entry.worldId)
            buffer.p1(entry.rank)
            buffer.pjstr(entry.worldName)
        }
    }
}
