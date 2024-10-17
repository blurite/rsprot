package net.rsprot.protocol.game.outgoing.codec.sound

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.game.outgoing.sound.MidiSongV1
import net.rsprot.protocol.message.codec.MessageEncoder

public class MidiSongV1Encoder : MessageEncoder<MidiSongV1> {
    override val prot: ServerProt = GameServerProt.MIDI_SONG_V1

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: MidiSongV1,
    ) {
        buffer.p2Alt1(message.id)
    }
}
