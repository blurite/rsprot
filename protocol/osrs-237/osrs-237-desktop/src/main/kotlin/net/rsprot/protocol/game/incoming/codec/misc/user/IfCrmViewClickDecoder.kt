package net.rsprot.protocol.game.incoming.codec.misc.user

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.misc.user.IfCrmViewClick
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.util.gCombinedIdAlt2

public class IfCrmViewClickDecoder : MessageDecoder<IfCrmViewClick> {
    override val prot: ClientProt = GameClientProt.IF_CRMVIEW_CLICK

    override fun decode(buffer: JagByteBuf): IfCrmViewClick {
        val serverTarget = buffer.g4Alt1()
        val sub = buffer.g2Alt2()
        val behaviour3 = buffer.g4Alt2()
        val behaviour2 = buffer.g4()
        val behaviour1 = buffer.g4Alt1()
        val combinedId = buffer.gCombinedIdAlt2()
        return IfCrmViewClick(
            serverTarget,
            combinedId,
            sub,
            behaviour1,
            behaviour2,
            behaviour3,
        )
    }
}
