package net.rsprot.protocol.game.outgoing.codec.zone.payload

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.internal.game.outgoing.codec.zone.payload.ZoneProtEncoder
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.game.outgoing.zone.payload.LocMerge

public class LocMergeEncoder : ZoneProtEncoder<LocMerge> {
    override val prot: ServerProt = GameServerProt.LOC_MERGE

    override fun encode(
        buffer: JagByteBuf,
        message: LocMerge,
    ) {
        // The function at the bottom of the LOC_MERGE has a consistent order,
        // making it easy to identify all the properties of this packet:
        // loc_merge(level, x, z, shape, rotation, layer, id, start, end, minX, minZ, maxX, maxZ, player)
        buffer.p1Alt3(message.minX)
        buffer.p1Alt3(message.minZ)
        buffer.p2Alt2(message.index)
        buffer.p2(message.id)
        buffer.p1Alt2(message.locPropertiesPacked)
        buffer.p1Alt3(message.maxX)
        buffer.p2Alt1(message.start)
        buffer.p2Alt1(message.end)
        buffer.p1(message.coordInZonePacked)
        buffer.p1Alt2(message.maxZ)
    }
}
