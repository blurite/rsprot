package net.rsprot.protocol.game.outgoing.codec.sound

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.game.outgoing.sound.AmbienceStop
import net.rsprot.protocol.message.codec.MessageEncoder

public class AmbienceStopEncoder : MessageEncoder<AmbienceStop> {
    override val prot: ServerProt = GameServerProt.AMBIENCE_STOP

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: AmbienceStop,
    ) {
        buffer.p1(if (message.fade) 1 else 0)
    }
}
