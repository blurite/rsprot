package net.rsprot.protocol.game.incoming.codec.social

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.game.incoming.social.IgnoreListDelMessage
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class IgnoreListDelDecoder : MessageDecoder<IgnoreListDelMessage> {
    override val prot: ClientProt = GameClientProt.IGNORELIST_DEL

    override fun decode(buffer: JagByteBuf): IgnoreListDelMessage {
        val name = buffer.gjstr()
        return IgnoreListDelMessage(name)
    }
}
