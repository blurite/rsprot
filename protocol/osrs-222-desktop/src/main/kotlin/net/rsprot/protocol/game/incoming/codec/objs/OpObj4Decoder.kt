package net.rsprot.protocol.game.incoming.codec.objs

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.objs.OpObj
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.tools.MessageDecodingTools

public class OpObj4Decoder : MessageDecoder<OpObj> {
    override val prot: ClientProt = GameClientProt.OPOBJ4

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): OpObj {
        val x = buffer.g2()
        val id = buffer.g2Alt1()
        val z = buffer.g2Alt2()
        val controlKey = buffer.g1Alt1() == 1
        return OpObj(
            id,
            x,
            z,
            controlKey,
            4,
        )
    }
}
