package net.rsprot.protocol.game.incoming.codec.npcs

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.npcs.OpNpcEvent
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder

public class OpNpc1Decoder : MessageDecoder<OpNpcEvent> {
    override val prot: ClientProt = GameClientProt.OPNPC1

    override fun decode(buffer: JagByteBuf): OpNpcEvent {
        val index = buffer.g2Alt2()
        val controlKey = buffer.g1Alt3() == 1
        return OpNpcEvent(
            index,
            controlKey,
            1,
        )
    }
}
