package net.rsprot.protocol.game.incoming.codec.npcs

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.npcs.OpNpcV2
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder

public class OpNpc2V2Decoder : MessageDecoder<OpNpcV2> {
    override val prot: ClientProt = GameClientProt.OPNPC2_V2

    override fun decode(buffer: JagByteBuf): OpNpcV2 {
        val controlKey = buffer.g1Alt1() == 1
        val subop = buffer.g1Alt1()
        val index = buffer.g2Alt3()
        return OpNpcV2(
            index,
            controlKey,
            2,
            subop,
        )
    }
}
