package net.rsprot.protocol.game.incoming.codec.buttons

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.buttons.IfButtonT
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.util.gCombinedId
import net.rsprot.protocol.util.gCombinedIdAlt1

public class IfButtonTDecoder : MessageDecoder<IfButtonT> {
    override val prot: ClientProt = GameClientProt.IF_BUTTONT

    override fun decode(buffer: JagByteBuf): IfButtonT {
        val targetCombinedId = buffer.gCombinedIdAlt1()
        val selectedSub = buffer.g2Alt3()
        val targetSub = buffer.g2Alt3()
        val selectedObj = buffer.g2()
        val selectedCombinedId = buffer.gCombinedId()
        val targetObj = buffer.g2Alt3()
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
