package net.rsprot.protocol.game.incoming.codec.npcs

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.npcs.OpNpcT
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.tools.MessageDecodingTools
import net.rsprot.protocol.util.gCombinedId

public class OpNpcTDecoder : MessageDecoder<OpNpcT> {
    override val prot: ClientProt = GameClientProt.OPNPCT

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): OpNpcT {
        val selectedCombinedId = buffer.gCombinedId()
        val controlKey = buffer.g1Alt1() == 1
        val selectedSub = buffer.g2Alt1()
        val selectedObj = buffer.g2Alt1()
        val index = buffer.g2Alt2()
        return OpNpcT(
            index,
            controlKey,
            selectedCombinedId,
            selectedSub,
            selectedObj,
        )
    }
}
