package net.rsprot.protocol.game.incoming.codec.npcs

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.npcs.OpNpcMessage
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder

public class OpNpc4Decoder : MessageDecoder<OpNpcMessage> {
    override val prot: ClientProt = GameClientProt.OPNPC4

    override fun decode(buffer: JagByteBuf): OpNpcMessage {
        val controlKey = buffer.g1Alt2() == 1
        val index = buffer.g2Alt1()
        return OpNpcMessage(
            index,
            controlKey,
            4,
        )
    }
}
