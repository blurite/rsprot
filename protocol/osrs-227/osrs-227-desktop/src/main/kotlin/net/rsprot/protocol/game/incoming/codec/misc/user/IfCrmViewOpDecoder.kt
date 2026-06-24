package net.rsprot.protocol.game.incoming.codec.misc.user

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.misc.user.IfCrmViewOp
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.util.gCombinedIdAlt3

public class IfCrmViewOpDecoder : MessageDecoder<IfCrmViewOp> {
    override val prot: ClientProt = GameClientProt.IF_CRMVIEW_OP

    override fun decode(buffer: JagByteBuf): IfCrmViewOp {
        val behaviour1 = buffer.g4Alt3()
        val sub = buffer.g2Alt2()
        val combinedId = buffer.gCombinedIdAlt3()
        val behaviour3 = buffer.g4Alt2()
        val behaviour2 = buffer.g4Alt3()
        val serverTarget = buffer.g4Alt2()
        return IfCrmViewOp(
            serverTarget,
            combinedId,
            sub,
            behaviour1,
            behaviour2,
            behaviour3,
        )
    }
}
