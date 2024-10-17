package net.rsprot.protocol.game.incoming.codec.npcs

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.npcs.OpNpcT
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.util.gCombinedId

public class OpNpcTDecoder : MessageDecoder<OpNpcT> {
    override val prot: ClientProt = GameClientProt.OPNPCT

    override fun decode(buffer: JagByteBuf): OpNpcT {
        val controlKey = buffer.g1Alt2() == 1
        val selectedSub = buffer.g2Alt1()
        val selectedCombinedId = buffer.gCombinedId()
        val index = buffer.g2Alt3()
        val selectedObj = buffer.g2Alt3()
        return OpNpcT(
            index,
            controlKey,
            selectedCombinedId,
            selectedSub,
            selectedObj,
        )
    }
}
