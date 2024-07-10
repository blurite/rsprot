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
        when (val update = message.updateType) {
            is UpdateFriendChatChannelFullV1.JoinUpdate -> {
                buffer.pjstr(update.channelOwner)
                buffer.p8(update.channelNameBase37)
                buffer.p1(update.kickRank)
                buffer.p1(update.entries.size)
                for (entry in update.entries) {
                    buffer.pjstr(entry.name)
                    buffer.p2(entry.worldId)
                    buffer.p1(entry.rank)
                    buffer.pjstr(entry.worldName)
                }
            }
            UpdateFriendChatChannelFullV1.LeaveUpdate -> {
                // No-op, no updates
            }
        }
    }
}
