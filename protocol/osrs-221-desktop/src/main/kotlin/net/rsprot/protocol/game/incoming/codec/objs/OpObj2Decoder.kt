package net.rsprot.protocol.game.incoming.codec.objs

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.objs.OpObjMessage
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder

public class OpObj2Decoder : MessageDecoder<OpObjMessage> {
    override val prot: ClientProt = GameClientProt.OPOBJ2

    override fun decode(buffer: JagByteBuf): OpObjMessage {
        val z = buffer.g2Alt3()
        val id = buffer.g2Alt1()
        val controlKey = buffer.g1Alt2() == 1
        val x = buffer.g2Alt3()
        return OpObjMessage(
            id,
            x,
            z,
            controlKey,
            2,
        )
    }
}
