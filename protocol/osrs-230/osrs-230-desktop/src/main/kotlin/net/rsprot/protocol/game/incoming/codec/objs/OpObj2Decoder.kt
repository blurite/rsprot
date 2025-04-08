package net.rsprot.protocol.game.incoming.codec.objs

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.objs.OpObj
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder

public class OpObj2Decoder : MessageDecoder<OpObj> {
    override val prot: ClientProt = GameClientProt.OPOBJ2

    override fun decode(buffer: JagByteBuf): OpObj {
        val controlKey = buffer.g1Alt3() == 1
        val z = buffer.g2Alt2()
        val x = buffer.g2Alt2()
        val id = buffer.g2()
        return OpObj(
            id,
            x,
            z,
            controlKey,
            2,
        )
    }
}
