package net.rsprot.protocol.game.incoming.codec.npcs

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.npcs.OpNpcT
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.util.gCombinedIdAlt1

public class OpNpcTDecoder : MessageDecoder<OpNpcT> {
    override val prot: ClientProt = GameClientProt.OPNPCT

    override fun decode(buffer: JagByteBuf): OpNpcT {
        val selectedSub = buffer.g2()
        val selectedObj = buffer.g2Alt1()
        val index = buffer.g2()
        val selectedCombinedId = buffer.gCombinedIdAlt1()
        val controlKey = buffer.g1Alt3() == 1
        return OpNpcT(
            index,
            controlKey,
            selectedCombinedId,
            selectedSub,
            selectedObj,
        )
    }
}
