package net.rsprot.protocol.game.incoming.codec.objs

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.objs.OpObjT
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.util.gCombinedIdAlt3

public class OpObjTDecoder : MessageDecoder<OpObjT> {
    override val prot: ClientProt = GameClientProt.OPOBJT

    override fun decode(buffer: JagByteBuf): OpObjT {
        val x = buffer.g2()
        val selectedObj = buffer.g2Alt1()
        val id = buffer.g2Alt1()
        val z = buffer.g2Alt1()
        val controlKey = buffer.g1Alt1() == 1
        val selectedCombinedId = buffer.gCombinedIdAlt3()
        val selectedSub = buffer.g2()
        return OpObjT(
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
