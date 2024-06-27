package net.rsprot.protocol.game.incoming.codec.objs

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.objs.OpObj6
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.tools.MessageDecodingTools

public class OpObj6Decoder : MessageDecoder<OpObj6> {
    override val prot: ClientProt = GameClientProt.OPOBJ6

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): OpObj6 {
        val x = buffer.g2Alt2()
        val id = buffer.g2()
        val z = buffer.g2()
        return OpObj6(
            id,
            x,
            z,
        )
    }
}
