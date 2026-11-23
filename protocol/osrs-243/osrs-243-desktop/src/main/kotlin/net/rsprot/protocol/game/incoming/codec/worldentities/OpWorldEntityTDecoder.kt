package net.rsprot.protocol.game.incoming.codec.worldentities

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.game.incoming.worldentities.OpWorldEntityT
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.util.gCombinedIdAlt3

public class OpWorldEntityTDecoder : MessageDecoder<OpWorldEntityT> {
    override val prot: ClientProt = GameClientProt.OPWORLDENTITYT

    override fun decode(buffer: JagByteBuf): OpWorldEntityT {
        val selectedCombinedId = buffer.gCombinedIdAlt3()
        val controlKey = buffer.g1Alt1() == 1
        val selectedSub = buffer.g2()
        val index = buffer.g2Alt2()
        val selectedObj = buffer.g2()
        return OpWorldEntityT(
            index,
            controlKey,
            selectedCombinedId,
            selectedSub,
            selectedObj,
        )
    }
}
