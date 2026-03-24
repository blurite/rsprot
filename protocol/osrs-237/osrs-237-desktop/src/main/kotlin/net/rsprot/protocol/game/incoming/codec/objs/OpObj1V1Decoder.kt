package net.rsprot.protocol.game.incoming.codec.objs

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.objs.OpObjV1
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder

public class OpObj1V1Decoder : MessageDecoder<OpObjV1> {
    override val prot: ClientProt = GameClientProt.OPOBJ1_V1

    override fun decode(buffer: JagByteBuf): OpObjV1 {
        val x = buffer.g2Alt3()
        val z = buffer.g2Alt2()
        val id = buffer.g2Alt2()
        val controlKey = buffer.g1Alt2() == 1
        return OpObjV1(
            id,
            x,
            z,
            controlKey,
            1,
        )
    }
}
