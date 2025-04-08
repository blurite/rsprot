package net.rsprot.protocol.game.outgoing.codec.zone.payload

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.game.outgoing.zone.payload.ObjCustomise
import net.rsprot.protocol.internal.game.outgoing.codec.zone.payload.ZoneProtEncoder

public class ObjCustomiseEncoder : ZoneProtEncoder<ObjCustomise> {
    override val prot: ServerProt = GameServerProt.OBJ_CUSTOMISE

    override fun encode(
        buffer: JagByteBuf,
        message: ObjCustomise,
    ) {
        // The function at the bottom of the OBJ_CUSTOMISE has a consistent order,
        // making it easy to identify all the properties of this packet:
        // objCustomise(level, x, z, id, count, recol, recolIndex, retex, retexIndex, model);
        buffer.p2(message.model)
        buffer.p2(message.recolIndex)
        buffer.p2Alt2(message.id)
        buffer.p1Alt3(message.coordInZonePacked)
        buffer.p2(message.retex)
        buffer.p4Alt3(message.quantity)
        buffer.p2(message.retexIndex)
        buffer.p2Alt1(message.recol)
    }
}
