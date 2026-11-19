package net.rsprot.protocol.game.incoming.codec.locs

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.locs.OpLocTV2
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.util.gCombinedIdAlt2

public class OpLocTV2Decoder : MessageDecoder<OpLocTV2> {
    override val prot: ClientProt = GameClientProt.OPLOCT_V2

    override fun decode(buffer: JagByteBuf): OpLocTV2 {
        val x = buffer.g2Alt1()
        val selectedCombinedId = buffer.gCombinedIdAlt2()
        val z = buffer.g2Alt1()
        val controlKey = buffer.g1() == 1
        val selectedObj = buffer.g2Alt3()
        val id = buffer.g3Alt2()
        val selectedSub = buffer.g2Alt2()
        return OpLocTV2(
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
