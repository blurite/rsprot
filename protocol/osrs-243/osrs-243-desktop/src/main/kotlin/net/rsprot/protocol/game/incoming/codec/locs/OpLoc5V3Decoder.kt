package net.rsprot.protocol.game.incoming.codec.locs

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.locs.OpLocV3
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder

public class OpLoc5V3Decoder : MessageDecoder<OpLocV3> {
    override val prot: ClientProt = GameClientProt.OPLOC5_V3

    override fun decode(buffer: JagByteBuf): OpLocV3 {
        val subop = buffer.g1Alt1()
        val controlKey = buffer.g1Alt1() == 1
        val id = buffer.g3()
        val x = buffer.g2Alt1()
        val z = buffer.g2Alt2()
        return OpLocV3(
            id,
            x,
            z,
            controlKey,
            5,
            subop,
        )
    }
}
