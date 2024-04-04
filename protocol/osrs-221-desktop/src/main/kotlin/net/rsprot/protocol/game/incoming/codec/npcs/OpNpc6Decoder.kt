package net.rsprot.protocol.game.incoming.codec.npcs

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.npcs.OpNpc6Event
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder

public class OpNpc6Decoder : MessageDecoder<OpNpc6Event> {
    override val prot: ClientProt = GameClientProt.OPNPC6

    override fun decode(buffer: JagByteBuf): OpNpc6Event {
        val id = buffer.g2Alt2()
        return OpNpc6Event(id)
    }
}
