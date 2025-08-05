package net.rsprot.protocol.game.outgoing.codec.sound

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.game.outgoing.sound.MidiJingle
import net.rsprot.protocol.message.codec.MessageEncoder

public class MidiJingleEncoder : MessageEncoder<MidiJingle> {
    override val prot: ServerProt = GameServerProt.MIDI_JINGLE

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: MidiJingle,
    ) {
        buffer.p2Alt3(message.id)
        buffer.p3Alt3(message.lengthInMillis)
    }
}
