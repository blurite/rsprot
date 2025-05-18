package net.rsprot.protocol.game.outgoing.codec.zone.payload

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.game.outgoing.zone.payload.SoundArea
import net.rsprot.protocol.internal.game.outgoing.codec.zone.payload.ZoneProtEncoder

public class SoundAreaEncoder : ZoneProtEncoder<SoundArea> {
    override val prot: ServerProt = GameServerProt.SOUND_AREA

    override fun encode(
        buffer: JagByteBuf,
        message: SoundArea,
    ) {
        // While the sound area packet doesn't have a static function call like
        // most of these other packets, one can still identify it with relative ease
        // using the screenshot below: https://media.z-kris.com/2024/04/0QX3RtlJF9.png
        buffer.p1Alt1(message.loops)
        buffer.p1Alt3(message.range)
        buffer.p1Alt3(message.delay)
        buffer.p1Alt1(message.coordInZonePacked)
        buffer.p1Alt1(message.dropOffRange)
        buffer.p2Alt1(message.id)
    }
}
