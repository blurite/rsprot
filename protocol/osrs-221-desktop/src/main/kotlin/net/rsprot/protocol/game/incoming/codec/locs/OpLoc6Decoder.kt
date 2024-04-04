package net.rsprot.protocol.game.incoming.codec.locs

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.locs.OpLoc6Message
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder

public class OpLoc6Decoder : MessageDecoder<OpLoc6Message> {
    override val prot: ClientProt = GameClientProt.OPLOC6

    override fun decode(buffer: JagByteBuf): OpLoc6Message {
        val id = buffer.g2Alt2()
        return OpLoc6Message(id)
    }
}
