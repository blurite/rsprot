package net.rsprot.protocol.game.outgoing.codec.zone.payload

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.common.game.outgoing.codec.zone.payload.ZoneProtEncoder
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.game.outgoing.zone.payload.ObjUncustomise

public class ObjUncustomiseEncoder : ZoneProtEncoder<ObjUncustomise> {
    override val prot: ServerProt = GameServerProt.OBJ_UNCUSTOMISE

    override fun encode(
        buffer: JagByteBuf,
        message: ObjUncustomise,
    ) {
        // The function at the bottom of the OBJ_CUSTOMISE has a consistent order,
        // making it easy to identify all the properties of this packet:
        // objUncustomise(level, x, z, id, count);
        buffer.p2Alt1(message.id)
        buffer.p1Alt3(message.coordInZonePacked)
        buffer.p4Alt1(message.quantity)
    }
}
