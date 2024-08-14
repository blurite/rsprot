package net.rsprot.protocol.game.incoming.codec.locs

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.locs.OpLocT
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.util.gCombinedIdAlt2

public class OpLocTDecoder : MessageDecoder<OpLocT> {
    override val prot: ClientProt = GameClientProt.OPLOCT

    override fun decode(buffer: JagByteBuf): OpLocT {
        val selectedSub = buffer.g2Alt1()
        val selectedCombinedId = buffer.gCombinedIdAlt2()
        val selectedObj = buffer.g2Alt2()
        val id = buffer.g2()
        val controlKey = buffer.g1() == 1
        val z = buffer.g2Alt1()
        val x = buffer.g2Alt2()
        return OpLocT(
            id,
            x,
            z,
            controlKey,
            selectedCombinedId,
            selectedSub,
            selectedObj,
        )
    }
}
