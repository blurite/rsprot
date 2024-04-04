package net.rsprot.protocol.game.incoming.codec.social

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.game.incoming.social.IgnoreListAddMessage
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent
import net.rsprot.protocol.tools.MessageDecodingTools

@Consistent
public class IgnoreListAddDecoder : MessageDecoder<IgnoreListAddMessage> {
    override val prot: ClientProt = GameClientProt.IGNORELIST_ADD

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): IgnoreListAddMessage {
        val name = buffer.gjstr()
        return IgnoreListAddMessage(name)
    }
}
