package net.rsprot.protocol.game.incoming.codec.players

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.game.incoming.players.OpPlayer
import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.tools.MessageDecodingTools

public class OpPlayer7Decoder : MessageDecoder<OpPlayer> {
    override val prot: ClientProt = GameClientProt.OPPLAYER7

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): OpPlayer {
        val controlKey = buffer.g1Alt2() == 1
        val index = buffer.g2Alt1()
        return OpPlayer(
            index,
            controlKey,
            7,
        )
    }
}
