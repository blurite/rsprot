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
        buffer.p1(message.height)
        buffer.p2Alt3(message.id)
        buffer.p1Alt2(message.coordInZonePacked)
        buffer.p2Alt1(message.delay)
    }
}
