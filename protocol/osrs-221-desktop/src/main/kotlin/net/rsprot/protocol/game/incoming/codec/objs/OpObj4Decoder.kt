package net.rsprot.protocol.game.incoming.codec.objs

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.objs.OpObjMessage
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder

public class OpObj4Decoder : MessageDecoder<OpObjMessage> {
    override val prot: ClientProt = GameClientProt.OPOBJ4

    override fun decode(buffer: JagByteBuf): OpObjMessage {
        val controlKey = buffer.g1() == 1
        val z = buffer.g2()
        val id = buffer.g2Alt1()
        val x = buffer.g2Alt2()
        return OpObjMessage(
            id,
            x,
            z,
            controlKey,
            4,
        )
    }
}
