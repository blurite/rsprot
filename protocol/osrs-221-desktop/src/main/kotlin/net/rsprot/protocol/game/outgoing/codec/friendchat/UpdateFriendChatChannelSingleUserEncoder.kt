package net.rsprot.protocol.game.outgoing.codec.friendchat

import io.netty.channel.ChannelHandlerContext
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.friendchat.UpdateFriendChatChannelSingleUser
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class UpdateFriendChatChannelSingleUserEncoder : MessageEncoder<UpdateFriendChatChannelSingleUser> {
    override val prot: ServerProt = GameServerProt.UPDATE_FRIENDCHAT_CHANNEL_SINGLEUSER

    override fun encode(
        ctx: ChannelHandlerContext,
        buffer: JagByteBuf,
        message: UpdateFriendChatChannelSingleUser,
    ) {
        val user = message.user
        buffer.pjstr(user.name)
        buffer.p2(user.worldId)
        buffer.p1(user.rank)
        if (user is UpdateFriendChatChannelSingleUser.AddedFriendChatUser) {
            buffer.pjstr(user.worldName)
        }
    }
}
