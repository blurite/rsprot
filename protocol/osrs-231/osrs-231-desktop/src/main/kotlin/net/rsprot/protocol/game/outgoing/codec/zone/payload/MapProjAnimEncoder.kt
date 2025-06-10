package net.rsprot.protocol.game.outgoing.codec.zone.payload

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.game.outgoing.zone.payload.MapProjAnim
import net.rsprot.protocol.internal.game.outgoing.codec.zone.payload.ZoneProtEncoder

public class MapProjAnimEncoder : ZoneProtEncoder<MapProjAnim> {
    override val prot: ServerProt = GameServerProt.MAP_PROJANIM

    override fun encode(
        buffer: JagByteBuf,
        message: MapProjAnim,
    ) {
        // The constructor at the bottom of the MAP_PROJANIM has a consistent order,
        // making it easy to identify all the properties of this packet:
        // ClientProj(
        // startLevel, startX, startZ, startHeight, sourceIndex,
        // endLevel, endX, endZ, endHeight, targetIndex,
        // id, startTime, endTime, angle, progress)
        buffer.p1(message.coordInZonePacked)
        buffer.p1Alt2(message.deltaX)
        buffer.p1(message.startHeight)
        buffer.p2(message.id)
        buffer.p1Alt1(message.angle)
        buffer.p2(message.progress)
        buffer.p1Alt1(message.endHeight)
        buffer.p1Alt1(message.deltaZ)
        buffer.p3Alt3(message.targetIndex)
        buffer.p2(message.startTime)
        buffer.p2Alt2(message.endTime)
        buffer.p3Alt1(message.sourceIndex)
    }
}
