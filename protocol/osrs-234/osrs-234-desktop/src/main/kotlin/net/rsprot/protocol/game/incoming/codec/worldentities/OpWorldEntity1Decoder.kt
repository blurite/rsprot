package net.rsprot.protocol.game.incoming.codec.worldentities

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.game.incoming.worldentities.OpWorldEntity
import net.rsprot.protocol.message.codec.MessageDecoder

public class OpWorldEntity1Decoder : MessageDecoder<OpWorldEntity> {
    override val prot: ClientProt = GameClientProt.OPWORLDENTITY1

    override fun decode(buffer: JagByteBuf): OpWorldEntity {
        val controlKey = buffer.g1() == 1
        val index = buffer.g2Alt1()
        return OpWorldEntity(
            index,
            controlKey,
            1,
        )
    }
}
