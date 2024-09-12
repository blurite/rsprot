package net.rsprot.protocol.game.outgoing.codec.sound

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.game.outgoing.sound.MidiSongOld
import net.rsprot.protocol.message.codec.MessageEncoder

public class MidiSongOldEncoder : MessageEncoder<MidiSongOld> {
    override val prot: ServerProt = GameServerProt.MIDI_SONG_OLD

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: MidiSongOld,
    ) {
        buffer.p2(message.id)
    }
}
