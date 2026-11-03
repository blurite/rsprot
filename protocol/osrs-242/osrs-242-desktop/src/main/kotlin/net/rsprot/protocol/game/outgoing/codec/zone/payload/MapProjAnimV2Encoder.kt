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
        buffer.p3Alt2(message.sourceIndex)
        buffer.p2Alt3(message.endTime)
        buffer.p2Alt1(message.startHeight)
        buffer.p2(message.endHeight)
        buffer.p2Alt1(message.startTime)
        buffer.p2(message.id)
        buffer.p1Alt2(message.coordInZonePacked)
        buffer.p1Alt1(message.angle)
        buffer.p2Alt1(message.progress)
        buffer.p4(message.end.packed)
        buffer.p3Alt1(message.targetIndex)
    }
}
