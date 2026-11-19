package net.rsprot.protocol.game.incoming.codec.locs

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.locs.OpLoc6V2
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder

public class OpLoc6V2Decoder : MessageDecoder<OpLoc6V2> {
    override val prot: ClientProt = GameClientProt.OPLOC6_V2

    override fun decode(buffer: JagByteBuf): OpLoc6V2 {
        val id = buffer.g3Alt2()
        return OpLoc6V2(id)
    }
}
