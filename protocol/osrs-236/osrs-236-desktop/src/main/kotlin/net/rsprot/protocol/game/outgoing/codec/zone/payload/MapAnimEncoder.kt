package net.rsprot.protocol.game.outgoing.codec.zone.payload

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.game.outgoing.zone.payload.MapAnim
import net.rsprot.protocol.internal.game.outgoing.codec.zone.payload.ZoneProtEncoder

public class MapAnimEncoder : ZoneProtEncoder<MapAnim> {
    override val prot: ServerProt = GameServerProt.MAP_ANIM

    override fun encode(
        buffer: JagByteBuf,
        message: MapAnim,
    ) {
        // While MAP_ANIM does not have a common function like the rest,
        // the constructor for the SpotAnimation object itself has the following order:
        // SpotAnimation(world, id, level, fineX, fineZ, getGroundHeight(fineX, fineZ, level) - height, delay, cycle)
        buffer.p1(message.height)
        buffer.p2Alt1(message.delay)
        buffer.p2(message.id)
        buffer.p1Alt3(message.coordInZonePacked)
    }
}
