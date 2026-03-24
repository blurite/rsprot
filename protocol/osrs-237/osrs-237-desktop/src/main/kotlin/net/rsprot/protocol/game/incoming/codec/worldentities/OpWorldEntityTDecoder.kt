package net.rsprot.protocol.game.incoming.codec.worldentities

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.game.incoming.worldentities.OpWorldEntityT
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.util.gCombinedId

public class OpWorldEntityTDecoder : MessageDecoder<OpWorldEntityT> {
    override val prot: ClientProt = GameClientProt.OPWORLDENTITYT

    override fun decode(buffer: JagByteBuf): OpWorldEntityT {
        val selectedSub = buffer.g2()
        val selectedCombinedId = buffer.gCombinedId()
        val index = buffer.g2Alt3()
        val selectedObj = buffer.g2Alt2()
        val controlKey = buffer.g1Alt3() == 1
        return OpWorldEntityT(
            index,
            controlKey,
            selectedCombinedId,
            selectedSub,
            selectedObj,
        )
    }
}
