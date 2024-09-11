package net.rsprot.protocol.game.incoming.codec.players

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.players.OpPlayer
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder

public class OpPlayer3Decoder : MessageDecoder<OpPlayer> {
    override val prot: ClientProt = GameClientProt.OPPLAYER3

    override fun decode(buffer: JagByteBuf): OpPlayer {
        val controlKey = buffer.g1Alt1() == 1
        val index = buffer.g2Alt2()
        return OpPlayer(
            index,
            controlKey,
            3,
        )
    }
}
