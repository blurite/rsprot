package net.rsprot.protocol.game.outgoing.codec.sound

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.game.outgoing.sound.MidiSongWithSecondary
import net.rsprot.protocol.message.codec.MessageEncoder

public class MidiSongWithSecondaryEncoder : MessageEncoder<MidiSongWithSecondary> {
    override val prot: ServerProt = GameServerProt.MIDI_SONG_WITHSECONDARY

    override fun encode(
        streamCipher: StreamCipher,
        buffer: JagByteBuf,
        message: MidiSongWithSecondary,
    ) {
        // The order in the client remains the same for the function call at the end
        // of the packet, as (the ids list has primary id as the first song):
        // playSongList(ids, fadeOutDelay, fadeOutSpeed, fadeInDelay, fadeInSpeed)
        buffer.p2Alt1(message.fadeOutSpeed)
        buffer.p2(message.fadeOutDelay)
        buffer.p2(message.primaryId)
        buffer.p2(message.fadeInDelay)
        buffer.p2Alt1(message.fadeInSpeed)
        buffer.p2Alt1(message.secondaryId)
    }
}
