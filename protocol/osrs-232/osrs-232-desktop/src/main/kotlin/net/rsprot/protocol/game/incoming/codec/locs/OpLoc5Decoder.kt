package net.rsprot.protocol.game.incoming.codec.locs

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.locs.OpLoc
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder

public class OpLoc5Decoder : MessageDecoder<OpLoc> {
    override val prot: ClientProt = GameClientProt.OPLOC5

    override fun decode(buffer: JagByteBuf): OpLoc {
        val z = buffer.g2Alt2()
        val controlKey = buffer.g1Alt2() == 1
        val id = buffer.g2Alt3()
        val x = buffer.g2()
        return OpLoc(
            id,
            x,
            z,
            controlKey,
            5,
        )
    }
}
