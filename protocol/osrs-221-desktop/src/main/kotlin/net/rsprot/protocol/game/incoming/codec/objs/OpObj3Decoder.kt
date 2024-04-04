package net.rsprot.protocol.game.incoming.codec.objs

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.objs.OpObjEvent
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder

public class OpObj3Decoder : MessageDecoder<OpObjEvent> {
    override val prot: ClientProt = GameClientProt.OPOBJ3

    override fun decode(buffer: JagByteBuf): OpObjEvent {
        val x = buffer.g2Alt2()
        val id = buffer.g2Alt1()
        val z = buffer.g2Alt3()
        val controlKey = buffer.g1Alt2() == 1
        return OpObjEvent(
            id,
            x,
            z,
            controlKey,
            3,
        )
    }
}
