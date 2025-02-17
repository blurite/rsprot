package net.rsprot.protocol.game.incoming.codec.buttons

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.buttons.IfButtonD
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.util.gCombinedId

public class IfButtonDDecoder : MessageDecoder<IfButtonD> {
    override val prot: ClientProt = GameClientProt.IF_BUTTOND

    override fun decode(buffer: JagByteBuf): IfButtonD {
        val selectedObj = buffer.g2()
        val targetSub = buffer.g2Alt2()
        val selectedSub = buffer.g2Alt2()
        val targetCombinedId = buffer.gCombinedId()
        val targetObj = buffer.g2()
        val selectedCombinedId = buffer.gCombinedId()
        return IfButtonD(
            selectedCombinedId,
            selectedSub,
            selectedObj,
            targetCombinedId,
            targetSub,
            targetObj,
        )
    }
}
