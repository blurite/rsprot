package net.rsprot.protocol.game.incoming.codec.objs

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.objs.OpObj
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder

public class OpObj3Decoder : MessageDecoder<OpObj> {
    override val prot: ClientProt = GameClientProt.OPOBJ3

    override fun decode(buffer: JagByteBuf): OpObj {
        val controlKey = buffer.g1Alt2() == 1
        val id = buffer.g2()
        val x = buffer.g2Alt2()
        val z = buffer.g2Alt3()
        return OpObj(
            id,
            x,
            z,
            controlKey,
            3,
        )
    }
}
