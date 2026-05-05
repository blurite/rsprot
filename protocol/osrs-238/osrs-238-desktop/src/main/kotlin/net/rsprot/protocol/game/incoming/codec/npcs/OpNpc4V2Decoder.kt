package net.rsprot.protocol.game.incoming.codec.npcs

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.npcs.OpNpcV2
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder

public class OpNpc4V2Decoder : MessageDecoder<OpNpcV2> {
    override val prot: ClientProt = GameClientProt.OPNPC4_V2

    override fun decode(buffer: JagByteBuf): OpNpcV2 {
        val subop = buffer.g1Alt2()
        val index = buffer.g2Alt1()
        val controlKey = buffer.g1Alt2() == 1
        return OpNpcV2(
            index,
            controlKey,
            4,
            subop,
        )
    }
}
