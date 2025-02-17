package net.rsprot.protocol.game.incoming.codec.social

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.game.incoming.social.FriendListDel
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class FriendListDelDecoder : MessageDecoder<FriendListDel> {
    override val prot: ClientProt = GameClientProt.FRIENDLIST_DEL

    override fun decode(buffer: JagByteBuf): FriendListDel {
        val name = buffer.gjstr()
        return FriendListDel(name)
    }
}
