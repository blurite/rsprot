package net.rsprot.protocol.game.outgoing.codec.zone.payload

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.game.outgoing.zone.payload.ObjEnabledOps
import net.rsprot.protocol.internal.game.outgoing.codec.zone.payload.ZoneProtEncoder

public class ObjEnabledOpsEncoder : ZoneProtEncoder<ObjEnabledOps> {
    override val prot: ServerProt = GameServerProt.OBJ_ENABLED_OPS

    override fun encode(
        buffer: JagByteBuf,
        message: ObjEnabledOps,
    ) {
        // The function at the bottom of the OBJ_OPFILTER has a consistent order,
        // making it easy to identify all the properties of this packet:
        // obj_opfilter(level, x, z, id, opFlags)
        buffer.p1Alt3(message.opFlags.toInt())
        buffer.p2Alt2(message.id)
        buffer.p1Alt1(message.coordInZonePacked)
    }
}
