package net.rsprot.protocol.game.incoming.codec.objs

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.objs.OpObj6
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder

public class OpObj6Decoder : MessageDecoder<OpObj6> {
    override val prot: ClientProt = GameClientProt.OPOBJ6

    override fun decode(buffer: JagByteBuf): OpObj6 {
        val z = buffer.g2()
        val id = buffer.g2()
        val x = buffer.g2Alt3()
        return OpObj6(
            id,
            x,
            z,
        )
    }
}
