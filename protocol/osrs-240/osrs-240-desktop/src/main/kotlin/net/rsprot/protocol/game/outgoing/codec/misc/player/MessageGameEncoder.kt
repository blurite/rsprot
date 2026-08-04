package net.rsprot.protocol.game.outgoing.codec.misc.player

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.misc.player.MessageGame
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class MessageGameEncoder : MessageEncoder<MessageGame> {
    override val prot: ServerProt = GameServerProt.MESSAGE_GAME

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: MessageGame,
    ) {
        buffer.pSmart1or2(message.type)
        val name = message.name
        if (name != null) {
            buffer.p1(1)
            buffer.pjstr(name)
        } else {
            buffer.p1(0)
        }
        buffer.pjstr(message.message)
    }
}
