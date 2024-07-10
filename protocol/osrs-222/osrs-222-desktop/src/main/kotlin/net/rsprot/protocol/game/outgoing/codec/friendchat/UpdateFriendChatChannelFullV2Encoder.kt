package net.rsprot.protocol.game.outgoing.codec.friendchat

import io.netty.channel.ChannelHandlerContext
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.friendchat.UpdateFriendChatChannelFullV2
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class UpdateFriendChatChannelFullV2Encoder : MessageEncoder<UpdateFriendChatChannelFullV2> {
    override val prot: ServerProt = GameServerProt.UPDATE_FRIENDCHAT_CHANNEL_FULL_V2

    override fun encode(
        ctx: ChannelHandlerContext,
        buffer: JagByteBuf,
        message: UpdateFriendChatChannelFullV2,
    ) {
        buffer.pjstr(message.channelOwner)
        buffer.p8(message.channelNameBase37)
        buffer.p1(message.kickRank)
        buffer.pSmart1or2null(message.entries.size)
        for (entry in message.entries) {
            buffer.pjstr(entry.name)
            buffer.p2(entry.worldId)
            buffer.p1(entry.rank)
            buffer.pjstr(entry.worldName)
        }
    }
}
