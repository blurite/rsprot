package net.rsprot.protocol.game.incoming.codec

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.IfButtonTEvent
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.util.gCombinedIdAlt2

public class IfButtonTDecoder : MessageDecoder<IfButtonTEvent> {
    override val prot: ClientProt = GameClientProt.IF_BUTTONT

    override fun decode(buffer: JagByteBuf): IfButtonTEvent {
        val targetCombinedId = buffer.gCombinedIdAlt2()
        val sourceObj = buffer.g2Alt3()
        val targetSub = buffer.g2Alt3()
        val sourceCombinedId = buffer.gCombinedIdAlt2()
        val sourceSub = buffer.g2Alt1()
        val targetObj = buffer.g2Alt2()
        return IfButtonTEvent(
            sourceCombinedId,
            sourceSub,
            sourceObj,
            targetCombinedId,
            targetSub,
            targetObj,
        )
    }
}
