package net.rsprot.protocol.game.outgoing.codec.zone.payload

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.game.outgoing.zone.payload.ObjOpFilter
import net.rsprot.protocol.internal.game.outgoing.codec.zone.payload.ZoneProtEncoder

public class ObjOpFilterEncoder : ZoneProtEncoder<ObjOpFilter> {
    override val prot: ServerProt = GameServerProt.OBJ_OPFILTER

    override fun encode(
        buffer: JagByteBuf,
        message: ObjOpFilter,
    ) {
        // The function at the bottom of the OBJ_OPFILTER has a consistent order,
        // making it easy to identify all the properties of this packet:
        // obj_opfilter(level, x, z, id, opFlags)
        buffer.p1(message.opFlags.value)
        buffer.p2Alt3(message.id)
        buffer.p1Alt1(message.coordInZonePacked)
    }
}
