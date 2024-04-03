package net.rsprot.protocol.game.incoming.codec

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.IfButtonDEvent
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.util.gCombinedIdAlt2

public class IfButtonDDecoder : MessageDecoder<IfButtonDEvent> {
    override val prot: ClientProt = GameClientProt.IF_BUTTOND

    override fun decode(buffer: JagByteBuf): IfButtonDEvent {
        val sourceSub = buffer.g2()
        val targetSub = buffer.g2Alt2()
        val sourceCombinedId = buffer.gCombinedIdAlt2()
        val targetCombinedId = buffer.gCombinedIdAlt2()
        val sourceObj = buffer.g2Alt1()
        val targetObj = buffer.g2Alt3()
        return IfButtonDEvent(
            sourceCombinedId,
            sourceSub,
            sourceObj,
            targetCombinedId,
            targetSub,
            targetObj,
        )
    }
}
