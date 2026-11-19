package net.rsprot.protocol.game.incoming.codec.locs

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.locs.OpLocV3
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder

public class OpLoc4V3Decoder : MessageDecoder<OpLocV3> {
    override val prot: ClientProt = GameClientProt.OPLOC4_V3

    override fun decode(buffer: JagByteBuf): OpLocV3 {
        val z = buffer.g2()
        val controlKey = buffer.g1() == 1
        val x = buffer.g2()
        val subop = buffer.g1Alt3()
        val id = buffer.g3Alt2()
        return OpLocV3(
            id,
            x,
            z,
            controlKey,
            4,
            subop,
        )
    }
}
