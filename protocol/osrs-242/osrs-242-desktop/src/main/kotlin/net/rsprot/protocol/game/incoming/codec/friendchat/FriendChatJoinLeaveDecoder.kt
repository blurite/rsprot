package net.rsprot.protocol.game.incoming.codec.friendchat

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.friendchat.FriendChatJoinLeave
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class FriendChatJoinLeaveDecoder : MessageDecoder<FriendChatJoinLeave> {
    override val prot: ClientProt = GameClientProt.FRIENDCHAT_JOIN_LEAVE

    override fun decode(buffer: JagByteBuf): FriendChatJoinLeave {
        val name =
            if (!buffer.isReadable) {
                null
            } else {
                buffer.gjstr()
            }
        return FriendChatJoinLeave(name)
    }
}
