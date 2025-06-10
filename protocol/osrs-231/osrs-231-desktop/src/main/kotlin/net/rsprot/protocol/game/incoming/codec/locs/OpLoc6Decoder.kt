package net.rsprot.protocol.game.incoming.codec.locs

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.locs.OpLoc6
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder

public class OpLoc6Decoder : MessageDecoder<OpLoc6> {
    override val prot: ClientProt = GameClientProt.OPLOC6

    override fun decode(buffer: JagByteBuf): OpLoc6 {
        val id = buffer.g2Alt1()
        return OpLoc6(id)
    }
}
