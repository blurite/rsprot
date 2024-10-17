package net.rsprot.protocol.game.outgoing.codec.zone.payload

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.common.game.outgoing.codec.zone.payload.ZoneProtEncoder
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.game.outgoing.zone.payload.ObjCustomise

public class ObjCustomiseEncoder : ZoneProtEncoder<ObjCustomise> {
    override val prot: ServerProt = GameServerProt.OBJ_CUSTOMISE

    override fun encode(
        buffer: JagByteBuf,
        message: ObjCustomise,
    ) {
        // The function at the bottom of the OBJ_CUSTOMISE has a consistent order,
        // making it easy to identify all the properties of this packet:
        // objCustomise(level, x, z, id, count, recol, recolIndex, retex, retexIndex, model);
        buffer.p1(message.coordInZonePacked)
        buffer.p2(message.model)
        buffer.p2Alt1(message.retex)
        buffer.p4Alt1(message.quantity)
        buffer.p2Alt2(message.id)
        buffer.p2(message.recol)
        buffer.p2Alt3(message.retexIndex)
        buffer.p2Alt1(message.recolIndex)
    }
}