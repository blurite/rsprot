package net.rsprot.protocol.game.incoming.codec.locs

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.locs.OpLocTV1
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.util.gCombinedIdAlt2

public class OpLocTV1Decoder : MessageDecoder<OpLocTV1> {
    override val prot: ClientProt = GameClientProt.OPLOCT_V1

    override fun decode(buffer: JagByteBuf): OpLocTV1 {
        val selectedObj = buffer.g2Alt1()
        val x = buffer.g2Alt1()
        val z = buffer.g2Alt2()
        val id = buffer.g2Alt3()
        val selectedCombinedId = buffer.gCombinedIdAlt2()
        val selectedSub = buffer.g2Alt1()
        val controlKey = buffer.g1Alt1() == 1
        return OpLocTV1(
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
