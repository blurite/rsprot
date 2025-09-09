package net.rsprot.protocol.game.incoming.codec.worldentities

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.game.incoming.worldentities.OpWorldEntity6
import net.rsprot.protocol.message.codec.MessageDecoder

public class OpWorldEntity6Decoder : MessageDecoder<OpWorldEntity6> {
    override val prot: ClientProt = GameClientProt.OPWORLDENTITY6

    override fun decode(buffer: JagByteBuf): OpWorldEntity6 {
        val id = buffer.g2Alt1()
        return OpWorldEntity6(id)
    }
}
