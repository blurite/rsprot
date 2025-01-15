package net.rsprot.protocol.game.incoming.codec.social

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.game.incoming.social.IgnoreListAdd
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class IgnoreListAddDecoder : MessageDecoder<IgnoreListAdd> {
    override val prot: ClientProt = GameClientProt.IGNORELIST_ADD

    override fun decode(buffer: JagByteBuf): IgnoreListAdd {
        val name = buffer.gjstr()
        return IgnoreListAdd(name)
    }
}
