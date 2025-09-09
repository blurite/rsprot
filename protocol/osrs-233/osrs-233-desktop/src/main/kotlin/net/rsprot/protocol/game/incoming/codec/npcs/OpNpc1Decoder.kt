package net.rsprot.protocol.game.incoming.codec.npcs

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.npcs.OpNpc
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder

public class OpNpc1Decoder : MessageDecoder<OpNpc> {
    override val prot: ClientProt = GameClientProt.OPNPC1

    override fun decode(buffer: JagByteBuf): OpNpc {
        val index = buffer.g2Alt2()
        val controlKey = buffer.g1Alt3() == 1
        return OpNpc(
            index,
            controlKey,
            1,
        )
    }
}
