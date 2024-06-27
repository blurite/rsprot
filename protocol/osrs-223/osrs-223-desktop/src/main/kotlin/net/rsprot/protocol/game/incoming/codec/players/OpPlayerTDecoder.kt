package net.rsprot.protocol.game.incoming.codec.players

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.players.OpPlayerT
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.tools.MessageDecodingTools
import net.rsprot.protocol.util.gCombinedIdAlt3

public class OpPlayerTDecoder : MessageDecoder<OpPlayerT> {
    override val prot: ClientProt = GameClientProt.OPPLAYERT

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): OpPlayerT {
        val selectedObj = buffer.g2()
        val selectedSub = buffer.g2Alt3()
        val index = buffer.g2()
        val combinedId = buffer.gCombinedIdAlt3()
        val controlKey = buffer.g1Alt1() == 1
        return OpPlayerT(
            index,
            controlKey,
            combinedId,
            selectedSub,
            selectedObj,
        )
    }
}
