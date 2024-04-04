package net.rsprot.protocol.game.incoming.codec.players

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.players.OpPlayerEvent
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder

public class OpPlayer1Decoder : MessageDecoder<OpPlayerEvent> {
    override val prot: ClientProt = GameClientProt.OPPLAYER1

    override fun decode(buffer: JagByteBuf): OpPlayerEvent {
        val index = buffer.g2Alt1()
        val controlKey = buffer.g1() == 1
        return OpPlayerEvent(
            index,
            controlKey,
            1,
        )
    }
}
