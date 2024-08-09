package net.rsprot.protocol.game.incoming.codec.players

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.players.OpPlayerT
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.util.gCombinedId

public class OpPlayerTDecoder : MessageDecoder<OpPlayerT> {
    override val prot: ClientProt = GameClientProt.OPPLAYERT

    override fun decode(buffer: JagByteBuf): OpPlayerT {
        val combinedId = buffer.gCombinedId()
        val controlKey = buffer.g1() == 1
        val selectedObj = buffer.g2()
        val selectedSub = buffer.g2()
        val index = buffer.g2Alt1()
        return OpPlayerT(
            index,
            controlKey,
            combinedId,
            selectedSub,
            selectedObj,
        )
    }
}
