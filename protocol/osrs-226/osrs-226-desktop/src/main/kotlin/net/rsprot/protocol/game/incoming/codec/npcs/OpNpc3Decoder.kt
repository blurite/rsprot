package net.rsprot.protocol.game.incoming.codec.npcs

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.npcs.OpNpc
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder

public class OpNpc3Decoder : MessageDecoder<OpNpc> {
    override val prot: ClientProt = GameClientProt.OPNPC3

    override fun decode(buffer: JagByteBuf): OpNpc {
        val controlKey = buffer.g1Alt2() == 1
        val index = buffer.g2Alt3()
        return OpNpc(
            index,
            controlKey,
            3,
        )
    }
}
