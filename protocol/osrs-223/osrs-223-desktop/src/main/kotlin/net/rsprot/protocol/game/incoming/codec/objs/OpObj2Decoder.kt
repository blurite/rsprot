package net.rsprot.protocol.game.incoming.codec.objs

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.objs.OpObj
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.tools.MessageDecodingTools

public class OpObj2Decoder : MessageDecoder<OpObj> {
    override val prot: ClientProt = GameClientProt.OPOBJ2

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): OpObj {
        val id = buffer.g2Alt3()
        val controlKey = buffer.g1Alt2() == 1
        val x = buffer.g2Alt3()
        val z = buffer.g2Alt1()
        return OpObj(
            id,
            x,
            z,
            controlKey,
            2,
        )
    }
}
