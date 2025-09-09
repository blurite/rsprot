package net.rsprot.protocol.game.outgoing.codec.zone.payload

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.game.outgoing.zone.payload.MapProjAnimV2
import net.rsprot.protocol.internal.game.outgoing.codec.zone.payload.ZoneProtEncoder

public class MapProjAnimV2Encoder : ZoneProtEncoder<MapProjAnimV2> {
    override val prot: ServerProt = GameServerProt.MAP_PROJANIM_V2

    override fun encode(
        buffer: JagByteBuf,
        message: MapProjAnimV2,
    ) {
        // The constructor at the bottom of the MAP_PROJANIM has a consistent order,
        // making it easy to identify all the properties of this packet:
        // ClientProj(
        // startLevel, startX, startZ, startHeight, sourceIndex,
        // endLevel, endX, endZ, endHeight, targetIndex,
        // id, startTime, endTime, angle, progress)
        buffer.p2Alt3(message.id)
        buffer.p2Alt2(message.endHeight)
        buffer.p3(message.sourceIndex)
        buffer.p2Alt2(message.progress)
        buffer.p3Alt2(message.targetIndex)
        buffer.p4Alt3(message.end.packed)
        buffer.p2Alt2(message.startTime)
        buffer.p2Alt1(message.startHeight)
        buffer.p2Alt3(message.endTime)
        buffer.p1Alt2(message.angle)
        buffer.p1Alt2(message.coordInZonePacked)
    }
}
