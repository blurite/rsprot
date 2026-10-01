package net.rsprot.protocol.game.outgoing.codec.zone.payload

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.game.outgoing.zone.payload.LocMerge
import net.rsprot.protocol.internal.game.outgoing.codec.zone.payload.ZoneProtEncoder

public class LocMergeEncoder : ZoneProtEncoder<LocMerge> {
    override val prot: ServerProt = GameServerProt.LOC_MERGE

    override fun encode(
        buffer: JagByteBuf,
        message: LocMerge,
    ) {
        buffer.p1Alt3(message.coordInZonePacked)
        buffer.p1(message.maxZ)
        buffer.p2(message.end)
        buffer.p1Alt1(message.minX)
        buffer.p1Alt3(message.locPropertiesPacked)
        buffer.p2Alt1(message.index)
        buffer.p2Alt3(message.start)
        buffer.p1Alt3(message.maxX)
        buffer.p2Alt3(message.id)
        buffer.p1Alt1(message.minZ)
    }
}
