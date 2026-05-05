package net.rsprot.protocol.game.incoming.codec.objs

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.objs.OpObjV2
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder

public class OpObj2V2Decoder : MessageDecoder<OpObjV2> {
    override val prot: ClientProt = GameClientProt.OPOBJ2_V2

    override fun decode(buffer: JagByteBuf): OpObjV2 {
        val controlKey = buffer.g1() == 1
        val id = buffer.g2()
        val x = buffer.g2Alt1()
        val subop = buffer.g1Alt3()
        val z = buffer.g2()
        return OpObjV2(
            id,
            x,
            z,
            controlKey,
            2,
            subop,
        )
    }
}
