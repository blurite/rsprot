package net.rsprot.protocol.game.incoming.codec.buttons

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.buttons.IfButtonT
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.util.gCombinedIdAlt1
import net.rsprot.protocol.util.gCombinedIdAlt2

public class IfButtonTDecoder : MessageDecoder<IfButtonT> {
    override val prot: ClientProt = GameClientProt.IF_BUTTONT

    override fun decode(buffer: JagByteBuf): IfButtonT {
        val selectedCombinedId = buffer.gCombinedIdAlt1()
        val targetSub = buffer.g2()
        val targetCombinedId = buffer.gCombinedIdAlt2()
        val selectedSub = buffer.g2Alt3()
        val targetObj = buffer.g2Alt3()
        val selectedObj = buffer.g2()
        return IfButtonT(
            selectedCombinedId,
            selectedSub,
            selectedObj,
            targetCombinedId,
            targetSub,
            targetObj,
        )
    }
}
