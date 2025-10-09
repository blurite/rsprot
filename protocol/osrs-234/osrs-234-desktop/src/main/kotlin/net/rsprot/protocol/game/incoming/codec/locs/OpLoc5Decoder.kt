package net.rsprot.protocol.game.incoming.codec.locs

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.locs.OpLoc
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder

public class OpLoc5Decoder : MessageDecoder<OpLoc> {
    override val prot: ClientProt = GameClientProt.OPLOC5

    override fun decode(buffer: JagByteBuf): OpLoc {
        val id = buffer.g2Alt1()
        val z = buffer.g2Alt3()
        val x = buffer.g2Alt1()
        val controlKey = buffer.g1Alt1() == 1
        return OpLoc(
            id,
            x,
            z,
            controlKey,
            5,
        )
    }
}
