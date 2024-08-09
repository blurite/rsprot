package net.rsprot.protocol.game.outgoing.codec.sound

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.game.outgoing.sound.SynthSound
import net.rsprot.protocol.message.codec.MessageEncoder
import net.rsprot.protocol.metadata.Consistent

@Consistent
public class SynthSoundEncoder : MessageEncoder<SynthSound> {
    override val prot: ServerProt = GameServerProt.SYNTH_SOUND

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: SynthSound,
    ) {
        buffer.p2(message.id)
        buffer.p1(message.loops)
        buffer.p2(message.delay)
    }
}
