package net.rsprot.protocol.game.incoming.codec.npcs

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.npcs.OpNpcV1
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder

public class OpNpc4V1Decoder : MessageDecoder<OpNpcV1> {
    override val prot: ClientProt = GameClientProt.OPNPC4_V1

    override fun decode(buffer: JagByteBuf): OpNpcV1 {
        val controlKey = buffer.g1Alt2() == 1
        val index = buffer.g2()
        return OpNpcV1(
            index,
            controlKey,
            4,
        )
    }
}
