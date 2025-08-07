package net.rsprot.protocol.game.incoming.codec.npcs

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.npcs.OpNpc
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder

public class OpNpc5Decoder : MessageDecoder<OpNpc> {
    override val prot: ClientProt = GameClientProt.OPNPC5

    override fun decode(buffer: JagByteBuf): OpNpc {
        val index = buffer.g2Alt1()
        val controlKey = buffer.g1Alt1() == 1
        return OpNpc(
            index,
            controlKey,
            5,
        )
    }
}
