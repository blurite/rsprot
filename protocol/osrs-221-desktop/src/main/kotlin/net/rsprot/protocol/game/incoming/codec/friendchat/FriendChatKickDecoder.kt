package net.rsprot.protocol.game.incoming.codec.friendchat

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.friendchat.FriendChatKickMessage
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent
import net.rsprot.protocol.tools.MessageDecodingTools

@Consistent
public class FriendChatKickDecoder : MessageDecoder<FriendChatKickMessage> {
    override val prot: ClientProt = GameClientProt.FRIENDCHAT_KICK

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): FriendChatKickMessage {
        val name = buffer.gjstr()
        return FriendChatKickMessage(name)
    }
}
