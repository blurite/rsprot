package net.rsprot.protocol.game.incoming.codec.buttons

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.buttons.IfButtonT
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.util.gCombinedIdAlt2
import net.rsprot.protocol.util.gCombinedIdAlt3

public class IfButtonTDecoder : MessageDecoder<IfButtonT> {
    override val prot: ClientProt = GameClientProt.IF_BUTTONT

    override fun decode(buffer: JagByteBuf): IfButtonT {
        val targetSub = buffer.g2()
        val selectedObj = buffer.g2Alt1()
        val selectedSub = buffer.g2Alt2()
        val selectedCombinedId = buffer.gCombinedIdAlt2()
        val targetObj = buffer.g2Alt3()
        val targetCombinedId = buffer.gCombinedIdAlt3()
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
