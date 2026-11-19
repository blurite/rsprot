package net.rsprot.protocol.game.outgoing.codec.sound

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.game.outgoing.sound.MidiSwap
import net.rsprot.protocol.message.codec.MessageEncoder

public class MidiSwapEncoder : MessageEncoder<MidiSwap> {
    override val prot: ServerProt = GameServerProt.MIDI_SWAP

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: MidiSwap,
    ) {
        buffer.p2Alt1(message.fadeOutDelay)
        buffer.p2(message.fadeInSpeed)
        buffer.p2Alt1(message.fadeInDelay)
        buffer.p2(message.fadeOutSpeed)
    }
}
