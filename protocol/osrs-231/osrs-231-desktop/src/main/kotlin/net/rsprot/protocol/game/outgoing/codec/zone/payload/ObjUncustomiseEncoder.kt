package net.rsprot.protocol.game.outgoing.codec.zone.payload

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.game.outgoing.zone.payload.ObjUncustomise
import net.rsprot.protocol.internal.game.outgoing.codec.zone.payload.ZoneProtEncoder

public class ObjUncustomiseEncoder : ZoneProtEncoder<ObjUncustomise> {
    override val prot: ServerProt = GameServerProt.OBJ_UNCUSTOMISE

    override fun encode(
        buffer: JagByteBuf,
        message: ObjUncustomise,
    ) {
        // The function at the bottom of the OBJ_CUSTOMISE has a consistent order,
        // making it easy to identify all the properties of this packet:
        // objUncustomise(level, x, z, id, count);
        buffer.p4Alt3(message.quantity)
        buffer.p2Alt3(message.id)
        buffer.p1Alt3(message.coordInZonePacked)
    }
}
