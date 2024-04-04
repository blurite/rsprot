package net.rsprot.protocol.game.incoming.codec.locs

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.locs.OpLocEvent
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder

public class OpLoc3Decoder : MessageDecoder<OpLocEvent> {
    override val prot: ClientProt = GameClientProt.OPLOC3

    override fun decode(buffer: JagByteBuf): OpLocEvent {
        val x = buffer.g2()
        val z = buffer.g2()
        val id = buffer.g2Alt3()
        val controlKey = buffer.g1() == 1
        return OpLocEvent(
            id,
            x,
            z,
            controlKey,
            3,
        )
    }
}
