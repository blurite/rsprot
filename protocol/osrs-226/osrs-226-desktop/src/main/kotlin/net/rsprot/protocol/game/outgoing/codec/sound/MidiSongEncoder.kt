package net.rsprot.protocol.game.outgoing.codec.sound

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.game.outgoing.sound.MidiSong
import net.rsprot.protocol.message.codec.MessageEncoder

public class MidiSongEncoder : MessageEncoder<MidiSong> {
    override val prot: ServerProt = GameServerProt.MIDI_SONG

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: MidiSong,
    ) {
        // The order in the client remains the same for the function call at the end
        // of the packet, as:
        // playSongList(ids, fadeOutDelay, fadeOutSpeed, fadeInDelay, fadeInSpeed)
        buffer.p2Alt2(message.fadeOutDelay)
        buffer.p2Alt3(message.fadeOutSpeed)
        buffer.p2Alt1(message.fadeInDelay)
        buffer.p2Alt3(message.id)
        buffer.p2Alt2(message.fadeInSpeed)
    }
}
