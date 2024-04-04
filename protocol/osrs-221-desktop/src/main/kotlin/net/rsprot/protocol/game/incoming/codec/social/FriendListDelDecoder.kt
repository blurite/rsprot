package net.rsprot.protocol.game.incoming.codec.social

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.game.incoming.social.FriendListDelMessage
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent
import net.rsprot.protocol.tools.MessageDecodingTools

@Consistent
public class FriendListDelDecoder : MessageDecoder<FriendListDelMessage> {
    override val prot: ClientProt = GameClientProt.FRIENDLIST_DEL

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): FriendListDelMessage {
        val name = buffer.gjstr()
        return FriendListDelMessage(name)
    }
}
