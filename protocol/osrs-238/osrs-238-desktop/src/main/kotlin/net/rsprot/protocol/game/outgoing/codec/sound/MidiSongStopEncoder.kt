package net.rsprot.protocol.game.outgoing.codec.sound

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.game.outgoing.sound.MidiSongStop
import net.rsprot.protocol.message.codec.MessageEncoder

public class MidiSongStopEncoder : MessageEncoder<MidiSongStop> {
    override val prot: ServerProt = GameServerProt.MIDI_SONG_STOP

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: MidiSongStop,
    ) {
        // The order in the client remains the same for the function call at the end
        // of the packet, as:
        // fadeOut(fadeOutDelay, fadeOutSpeed)
        buffer.p2(message.fadeOutSpeed)
        buffer.p2Alt3(message.fadeOutDelay)
    }
}
