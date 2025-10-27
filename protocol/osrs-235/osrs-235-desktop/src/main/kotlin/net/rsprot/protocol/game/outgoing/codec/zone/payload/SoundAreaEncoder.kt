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
        // Sound area function can be found at the bottom as:
        // SoundList.playAreaSound(activeWorld.id, id, x, z, range, dropOffRange, loops, delay);
        buffer.p2Alt3(message.id)
        buffer.p1Alt2(message.delay)
        buffer.p1Alt1(message.range)
        buffer.p1Alt1(message.coordInZonePacked)
        buffer.p1Alt2(message.loops)
        buffer.p1(message.dropOffRange)
    }
}
