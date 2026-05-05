package net.rsprot.protocol.game.outgoing.codec.sound

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.game.outgoing.sound.AmbienceStart
import net.rsprot.protocol.message.codec.MessageEncoder

public class AmbienceStartEncoder : MessageEncoder<AmbienceStart> {
    override val prot: ServerProt = GameServerProt.AMBIENCE_START

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: AmbienceStart,
    ) {
        buffer.p1Alt1(if (message.fade) 1 else 0)
        buffer.p2(message.id)
    }
}
