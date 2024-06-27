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
        val index = buffer.g2Alt2()
        val controlKey = buffer.g1Alt1() == 1
        return OpPlayer(
            index,
            controlKey,
            7,
        )
    }
}
