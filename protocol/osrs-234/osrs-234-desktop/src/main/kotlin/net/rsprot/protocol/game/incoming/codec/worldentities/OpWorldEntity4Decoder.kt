package net.rsprot.protocol.game.incoming.codec.worldentities

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.game.incoming.worldentities.OpWorldEntity
import net.rsprot.protocol.message.codec.MessageDecoder

public class OpWorldEntity4Decoder : MessageDecoder<OpWorldEntity> {
    override val prot: ClientProt = GameClientProt.OPWORLDENTITY4

    override fun decode(buffer: JagByteBuf): OpWorldEntity {
        val index = buffer.g2Alt2()
        val controlKey = buffer.g1Alt3() == 1
        return OpWorldEntity(
            index,
            controlKey,
            4,
        )
    }
}
