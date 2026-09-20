package net.rsprot.protocol.game.outgoing.codec.sound

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.game.outgoing.sound.AmbientSoundStart
import net.rsprot.protocol.message.codec.MessageEncoder

public class AmbientSoundStartEncoder : MessageEncoder<AmbientSoundStart> {
    override val prot: ServerProt = GameServerProt.AMBIENTSOUND_START

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: AmbientSoundStart,
    ) {
        buffer.p2Alt2(message.id)
        buffer.p1Alt2(if (message.fade) 1 else 0)
    }
}
