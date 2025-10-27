package net.rsprot.protocol.game.incoming.codec.worldentities

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.game.incoming.worldentities.OpWorldEntity
import net.rsprot.protocol.message.codec.MessageDecoder

public class OpWorldEntity3Decoder : MessageDecoder<OpWorldEntity> {
    override val prot: ClientProt = GameClientProt.OPWORLDENTITY3

    override fun decode(buffer: JagByteBuf): OpWorldEntity {
        val controlKey = buffer.g1Alt1() == 1
        val index = buffer.g2Alt3()
        return OpWorldEntity(
            index,
            controlKey,
            3,
        )
    }
}
