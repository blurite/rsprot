package net.rsprot.protocol.game.outgoing.codec.sound

import io.netty.channel.ChannelHandlerContext
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.game.outgoing.sound.MidiJingle
import net.rsprot.protocol.message.codec.MessageEncoder

public class MidiJingleEncoder : MessageEncoder<MidiJingle> {
    override val prot: ServerProt = GameServerProt.MIDI_JINGLE

    override fun encode(
        ctx: ChannelHandlerContext,
        buffer: JagByteBuf,
        message: MidiJingle,
    ) {
        buffer.p2Alt2(message.id)
        buffer.p3Alt3(message.lengthInMillis)
    }
}
