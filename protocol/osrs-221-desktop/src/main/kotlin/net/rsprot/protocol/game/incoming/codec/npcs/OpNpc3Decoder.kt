package net.rsprot.protocol.game.incoming.codec.npcs

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.npcs.OpNpcMessage
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.tools.MessageDecodingTools

public class OpNpc3Decoder : MessageDecoder<OpNpcMessage> {
    override val prot: ClientProt = GameClientProt.OPNPC3

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): OpNpcMessage {
        val index = buffer.g2Alt3()
        val controlKey = buffer.g1() == 1
        return OpNpcMessage(
            index,
            controlKey,
            3,
        )
    }
}
