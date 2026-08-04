package net.rsprot.protocol.game.incoming.codec.npcs

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.npcs.OpNpcV2
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder

public class OpNpc4V2Decoder : MessageDecoder<OpNpcV2> {
    override val prot: ClientProt = GameClientProt.OPNPC4_V2

    override fun decode(buffer: JagByteBuf): OpNpcV2 {
        val controlKey = buffer.g1Alt3() == 1
        val subop = buffer.g1()
        val index = buffer.g2Alt2()
        return OpNpcV2(
            index,
            controlKey,
            4,
            subop,
        )
    }
}
