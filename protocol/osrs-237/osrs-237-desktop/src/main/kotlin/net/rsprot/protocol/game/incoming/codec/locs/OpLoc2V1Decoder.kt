package net.rsprot.protocol.game.incoming.codec.locs

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.locs.OpLocV1
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder

public class OpLoc2V1Decoder : MessageDecoder<OpLocV1> {
    override val prot: ClientProt = GameClientProt.OPLOC2_V1

    override fun decode(buffer: JagByteBuf): OpLocV1 {
        val controlKey = buffer.g1Alt2() == 1
        val x = buffer.g2Alt2()
        val z = buffer.g2Alt3()
        val id = buffer.g2Alt3()
        return OpLocV1(
            id,
            x,
            z,
            controlKey,
            2,
        )
    }
}
