package net.rsprot.protocol.game.incoming.codec.buttons

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.buttons.IfSubOp
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent
import net.rsprot.protocol.util.gCombinedId

@Consistent
public class IfSubOpDecoder : MessageDecoder<IfSubOp> {
    override val prot: ClientProt = GameClientProt.IF_SUBOP

    override fun decode(buffer: JagByteBuf): IfSubOp {
        val combinedId = buffer.gCombinedId()
        val sub = buffer.g2()
        val obj = buffer.g2()
        val subop = buffer.g1()
        val op = buffer.g1()
        return IfSubOp(
            combinedId,
            sub,
            obj,
            op + 1,
            subop,
        )
    }
}
