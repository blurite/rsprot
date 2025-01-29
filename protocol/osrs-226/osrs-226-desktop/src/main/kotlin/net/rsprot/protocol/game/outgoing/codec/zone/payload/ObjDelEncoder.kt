package net.rsprot.protocol.game.outgoing.codec.zone.payload

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.internal.game.outgoing.codec.zone.payload.ZoneProtEncoder
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.game.outgoing.zone.payload.ObjDel

public class ObjDelEncoder : ZoneProtEncoder<ObjDel> {
    override val prot: ServerProt = GameServerProt.OBJ_DEL

    override fun encode(
        buffer: JagByteBuf,
        message: ObjDel,
    ) {
        // The function at the bottom of the OBJ_DEL has a consistent order,
        // making it easy to identify all the properties of this packet:
        // obj_del(level, x, z, id, quantity)
        buffer.p1Alt3(message.coordInZonePacked)
        buffer.p2Alt2(message.id)
        buffer.p4Alt1(message.quantity)
    }
}
