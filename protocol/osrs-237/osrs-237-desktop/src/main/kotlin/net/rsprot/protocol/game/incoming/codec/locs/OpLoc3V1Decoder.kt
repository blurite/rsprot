package net.rsprot.protocol.game.incoming.codec.locs

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.locs.OpLocV1
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder

public class OpLoc3V1Decoder : MessageDecoder<OpLocV1> {
    override val prot: ClientProt = GameClientProt.OPLOC3_V1

    override fun decode(buffer: JagByteBuf): OpLocV1 {
        val id = buffer.g2Alt2()
        val z = buffer.g2Alt1()
        val x = buffer.g2()
        val controlKey = buffer.g1Alt2() == 1
        return OpLocV1(
            id,
            x,
            z,
            controlKey,
            3,
        )
    }
}
