package net.rsprot.protocol.game.incoming.codec.npcs

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.npcs.OpNpcV1
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder

public class OpNpc1V1Decoder : MessageDecoder<OpNpcV1> {
    override val prot: ClientProt = GameClientProt.OPNPC1_V1

    override fun decode(buffer: JagByteBuf): OpNpcV1 {
        val index = buffer.g2Alt1()
        val controlKey = buffer.g1Alt1() == 1
        return OpNpcV1(
            index,
            controlKey,
            1,
        )
    }
}
