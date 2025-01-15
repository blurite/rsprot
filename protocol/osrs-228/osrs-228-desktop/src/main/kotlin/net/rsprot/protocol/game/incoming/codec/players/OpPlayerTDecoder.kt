package net.rsprot.protocol.game.incoming.codec.players

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.players.OpPlayerT
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.util.gCombinedIdAlt2

public class OpPlayerTDecoder : MessageDecoder<OpPlayerT> {
    override val prot: ClientProt = GameClientProt.OPPLAYERT

    override fun decode(buffer: JagByteBuf): OpPlayerT {
        val selectedSub = buffer.g2()
        val selectedCombinedId = buffer.gCombinedIdAlt2()
        val selectedObj = buffer.g2Alt3()
        val controlKey = buffer.g1() == 1
        val index = buffer.g2()
        return OpPlayerT(
            index,
            controlKey,
            selectedCombinedId,
            selectedSub,
            selectedObj,
        )
    }
}
