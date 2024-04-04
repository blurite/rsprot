package net.rsprot.protocol.game.incoming.codec.objs

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.objs.OpObjTMessage
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.tools.MessageDecodingTools
import net.rsprot.protocol.util.gCombinedId

public class OpObjTDecoder : MessageDecoder<OpObjTMessage> {
    override val prot: ClientProt = GameClientProt.OPOBJT

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): OpObjTMessage {
        val selectedCombinedId = buffer.gCombinedId()
        val id = buffer.g2Alt1()
        val x = buffer.g2()
        val selectedSub = buffer.g2()
        val z = buffer.g2Alt3()
        val selectedObj = buffer.g2Alt3()
        val controlKey = buffer.g1() == 1
        return OpObjTMessage(
            id,
            x,
            z,
            controlKey,
            selectedCombinedId,
            selectedSub,
            selectedObj,
        )
    }
}
