package net.rsprot.protocol.game.incoming.codec.npcs

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.npcs.OpNpcEvent
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder

public class OpNpc2Decoder : MessageDecoder<OpNpcEvent> {
    override val prot: ClientProt = GameClientProt.OPNPC2

    override fun decode(buffer: JagByteBuf): OpNpcEvent {
        val controlKey = buffer.g1() == 1
        val index = buffer.g2Alt2()
        return OpNpcEvent(
            index,
            controlKey,
            2,
        )
    }
}
