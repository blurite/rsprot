package net.rsprot.protocol.game.incoming.codec.objs

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.objs.OpObjMessage
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.tools.MessageDecodingTools

public class OpObj1Decoder : MessageDecoder<OpObjMessage> {
    override val prot: ClientProt = GameClientProt.OPOBJ1

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): OpObjMessage {
        val controlKey = buffer.g1() == 1
        val x = buffer.g2Alt1()
        val id = buffer.g2Alt1()
        val z = buffer.g2Alt3()
        return OpObjMessage(
            id,
            x,
            z,
            controlKey,
            1,
        )
    }
}
