package net.rsprot.protocol.game.incoming.codec.friendchat

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.friendchat.FriendChatJoinLeaveMessage
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent
import net.rsprot.protocol.tools.MessageDecodingTools

@Consistent
public class FriendChatJoinLeaveDecoder : MessageDecoder<FriendChatJoinLeaveMessage> {
    override val prot: ClientProt = GameClientProt.FRIENDCHAT_JOIN_LEAVE

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): FriendChatJoinLeaveMessage {
        val name =
            if (!buffer.isReadable) {
                null
            } else {
                buffer.gjstr()
            }
        return FriendChatJoinLeaveMessage(name)
    }
}
