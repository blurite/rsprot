package net.rsprot.protocol.game.incoming.codec.locs

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.locs.OpLocV2
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder

public class OpLoc4V2Decoder : MessageDecoder<OpLocV2> {
    override val prot: ClientProt = GameClientProt.OPLOC4_V2

    override fun decode(buffer: JagByteBuf): OpLocV2 {
        val subop = buffer.g1Alt2()
        val x = buffer.g2Alt2()
        val controlKey = buffer.g1Alt1() == 1
        val id = buffer.g2Alt3()
        val z = buffer.g2Alt2()
        return OpLocV2(
            id,
            x,
            z,
            controlKey,
            4,
            subop,
        )
    }
}
