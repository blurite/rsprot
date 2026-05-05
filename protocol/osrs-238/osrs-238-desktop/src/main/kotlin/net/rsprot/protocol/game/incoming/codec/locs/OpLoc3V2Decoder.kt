package net.rsprot.protocol.game.incoming.codec.locs

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.locs.OpLocV2
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder

public class OpLoc3V2Decoder : MessageDecoder<OpLocV2> {
    override val prot: ClientProt = GameClientProt.OPLOC3_V2

    override fun decode(buffer: JagByteBuf): OpLocV2 {
        val z = buffer.g2Alt3()
        val controlKey = buffer.g1() == 1
        val x = buffer.g2Alt2()
        val id = buffer.g2Alt1()
        val subop = buffer.g1Alt1()
        return OpLocV2(
            id,
            x,
            z,
            controlKey,
            3,
            subop,
        )
    }
}
