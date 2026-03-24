package net.rsprot.protocol.game.incoming.codec.objs

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.objs.OpObj
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder

public class OpObj3Decoder : MessageDecoder<OpObj> {
    override val prot: ClientProt = GameClientProt.OPOBJ3

    override fun decode(buffer: JagByteBuf): OpObj {
        val id = buffer.g2Alt1()
        val z = buffer.g2Alt2()
        val controlKey = buffer.g1Alt2() == 1
        val x = buffer.g2()
        return OpObj(
            id,
            x,
            z,
            controlKey,
            3,
        )
    }
}
