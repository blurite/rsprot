package net.rsprot.protocol.game.incoming.codec.misc.user

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.misc.user.IfCrmViewOp
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.util.gCombinedIdAlt1

public class IfCrmViewOpDecoder : MessageDecoder<IfCrmViewOp> {
    override val prot: ClientProt = GameClientProt.IF_CRMVIEW_OP

    override fun decode(buffer: JagByteBuf): IfCrmViewOp {
        val combinedId = buffer.gCombinedIdAlt1()
        val sub = buffer.g2Alt1()
        val behaviour2 = buffer.g4Alt3()
        val serverTarget = buffer.g4Alt3()
        val behaviour1 = buffer.g4Alt1()
        val behaviour3 = buffer.g4Alt1()
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
