package net.rsprot.protocol.game.outgoing.codec.social

import io.netty.channel.ChannelHandlerContext
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.game.outgoing.social.UpdateFriendList
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class UpdateFriendListEncoder : MessageEncoder<UpdateFriendList> {
    override val prot: ServerProt = GameServerProt.UPDATE_FRIENDLIST

    override fun encode(
        ctx: ChannelHandlerContext,
        buffer: JagByteBuf,
        message: UpdateFriendList,
    ) {
        for (friend in message.friends) {
            buffer.p1(if (friend.added) 1 else 0)
            buffer.pjstr(friend.name)
            buffer.pjstr(friend.previousName ?: "")
            buffer.p2(friend.worldId)
            buffer.p1(friend.rank)
            buffer.p1(friend.properties)
            if (friend is UpdateFriendList.OnlineFriend) {
                buffer.pjstr(friend.worldName)
                buffer.p1(friend.platform)
                buffer.p4(friend.worldFlags)
            }
            buffer.pjstr(friend.notes)
        }
    }
}
