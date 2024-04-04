package net.rsprot.protocol.game.incoming.codec.npcs

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.npcs.OpNpcMessage
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.tools.MessageDecodingTools

public class OpNpc2Decoder : MessageDecoder<OpNpcMessage> {
    override val prot: ClientProt = GameClientProt.OPNPC2

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): OpNpcMessage {
        val controlKey = buffer.g1() == 1
        val index = buffer.g2Alt2()
        return OpNpcMessage(
            index,
            controlKey,
            2,
        )
    }
}
