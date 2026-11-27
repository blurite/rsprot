package net.rsprot.protocol.game.incoming.codec.locs

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.locs.OpLoc6V1
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder

public class OpLoc6V1Decoder : MessageDecoder<OpLoc6V1> {
    override val prot: ClientProt = GameClientProt.OPLOC6_V1

    override fun decode(buffer: JagByteBuf): OpLoc6V1 {
        val id = buffer.g2Alt1()
        return OpLoc6V1(id)
    }
}
