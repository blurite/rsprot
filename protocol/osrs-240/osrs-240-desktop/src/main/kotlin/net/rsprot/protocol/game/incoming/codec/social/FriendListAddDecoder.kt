package net.rsprot.protocol.game.incoming.codec.social

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.game.incoming.social.FriendListAdd
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class FriendListAddDecoder : MessageDecoder<FriendListAdd> {
    override val prot: ClientProt = GameClientProt.FRIENDLIST_ADD

    override fun decode(buffer: JagByteBuf): FriendListAdd {
        val name = buffer.gjstr()
        return FriendListAdd(name)
    }
}
