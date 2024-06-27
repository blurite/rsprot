package net.rsprot.protocol.game.incoming.codec.npcs

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.npcs.OpNpc6
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.tools.MessageDecodingTools

public class OpNpc6Decoder : MessageDecoder<OpNpc6> {
    override val prot: ClientProt = GameClientProt.OPNPC6

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): OpNpc6 {
        val id = buffer.g2Alt3()
        return OpNpc6(id)
    }
}
