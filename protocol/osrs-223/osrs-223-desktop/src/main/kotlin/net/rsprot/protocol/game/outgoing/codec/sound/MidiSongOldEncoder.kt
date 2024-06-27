package net.rsprot.protocol.game.outgoing.codec.sound

import io.netty.channel.ChannelHandlerContext
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.game.outgoing.sound.MidiSongOld
import net.rsprot.protocol.message.codec.MessageEncoder

public class MidiSongOldEncoder : MessageEncoder<MidiSongOld> {
    override val prot: ServerProt = GameServerProt.MIDI_SONG_OLD

    override fun encode(
        ctx: ChannelHandlerContext,
        buffer: JagByteBuf,
        message: MidiSongOld,
    ) {
        buffer.p2Alt2(message.id)
    }
}
