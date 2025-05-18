package net.rsprot.protocol.game.outgoing.codec.zone.payload

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.game.outgoing.zone.payload.ObjDel
import net.rsprot.protocol.internal.game.outgoing.codec.zone.payload.ZoneProtEncoder

public class ObjDelEncoder : ZoneProtEncoder<ObjDel> {
    override val prot: ServerProt = GameServerProt.OBJ_DEL

    override fun encode(
        buffer: JagByteBuf,
        message: ObjDel,
    ) {
        // The function at the bottom of the OBJ_DEL has a consistent order,
        // making it easy to identify all the properties of this packet:
        // obj_del(level, x, z, id, quantity)
        buffer.p2Alt1(message.id)
        buffer.p1Alt1(message.coordInZonePacked)
        buffer.p4(message.quantity)
    }
}
