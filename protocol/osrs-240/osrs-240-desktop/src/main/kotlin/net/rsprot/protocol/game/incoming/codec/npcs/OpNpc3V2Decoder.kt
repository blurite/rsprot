package net.rsprot.protocol.game.incoming.codec.npcs

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.npcs.OpNpcV2
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder

public class OpNpc3V2Decoder : MessageDecoder<OpNpcV2> {
    override val prot: ClientProt = GameClientProt.OPNPC3_V2

    override fun decode(buffer: JagByteBuf): OpNpcV2 {
        val controlKey = buffer.g1Alt2() == 1
        val subop = buffer.g1Alt3()
        val index = buffer.g2Alt2()
        return OpNpcV2(
            index,
            controlKey,
            3,
            subop,
        )
    }
}
