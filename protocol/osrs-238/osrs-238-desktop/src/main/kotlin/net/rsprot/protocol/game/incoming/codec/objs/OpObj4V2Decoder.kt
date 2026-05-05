package net.rsprot.protocol.game.incoming.codec.objs

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.objs.OpObjV2
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder

public class OpObj4V2Decoder : MessageDecoder<OpObjV2> {
    override val prot: ClientProt = GameClientProt.OPOBJ4_V2

    override fun decode(buffer: JagByteBuf): OpObjV2 {
        val z = buffer.g2Alt1()
        val controlKey = buffer.g1Alt2() == 1
        val subop = buffer.g1()
        val x = buffer.g2()
        val id = buffer.g2Alt2()
        return OpObjV2(
            id,
            x,
            z,
            controlKey,
            4,
            subop,
        )
    }
}
