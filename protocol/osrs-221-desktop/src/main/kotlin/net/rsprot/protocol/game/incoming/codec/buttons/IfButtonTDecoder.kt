package net.rsprot.protocol.game.incoming.codec.buttons

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.buttons.IfButtonTMessage
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.util.gCombinedIdAlt2

public class IfButtonTDecoder : MessageDecoder<IfButtonTMessage> {
    override val prot: ClientProt = GameClientProt.IF_BUTTONT

    override fun decode(buffer: JagByteBuf): IfButtonTMessage {
        val targetCombinedId = buffer.gCombinedIdAlt2()
        val selectedObj = buffer.g2Alt3()
        val targetSub = buffer.g2Alt3()
        val selectedCombinedId = buffer.gCombinedIdAlt2()
        val selectedSub = buffer.g2Alt1()
        val targetObj = buffer.g2Alt2()
        return IfButtonTMessage(
            selectedCombinedId,
            selectedSub,
            selectedObj,
            targetCombinedId,
            targetSub,
            targetObj,
        )
    }
}
