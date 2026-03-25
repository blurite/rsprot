package net.rsprot.protocol.game.incoming.codec.locs

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.locs.OpLocV1
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder

public class OpLoc5V1Decoder : MessageDecoder<OpLocV1> {
    override val prot: ClientProt = GameClientProt.OPLOC5_V1

    override fun decode(buffer: JagByteBuf): OpLocV1 {
        val id = buffer.g2Alt3()
        val controlKey = buffer.g1Alt3() == 1
        val z = buffer.g2Alt2()
        val x = buffer.g2Alt3()
        return OpLocV1(
            id,
            x,
            z,
            controlKey,
            5,
        )
    }
}
