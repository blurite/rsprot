package net.rsprot.protocol.game.outgoing.codec.zone.payload

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.common.game.outgoing.codec.zone.payload.ZoneProtEncoder
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.game.outgoing.zone.payload.ObjAdd

public class ObjAddEncoder : ZoneProtEncoder<ObjAdd> {
    override val prot: ServerProt = GameServerProt.OBJ_ADD

    override fun encode(
        buffer: JagByteBuf,
        message: ObjAdd,
    ) {
        // The function at the bottom of the OBJ_ADD has a consistent order,
        // making it easy to identify all the properties of this packet:
        // obj_add(level, x, z, id, quantity, opFlags,
        // timeUntilPublic, timeUntilDespawn, ownershipType, neverBecomesPublic)
        buffer.p2Alt1(message.timeUntilDespawn)
        buffer.p1Alt3(if (message.neverBecomesPublic) 1 else 0)
        buffer.p2Alt2(message.id)
        buffer.p1Alt1(message.opFlags.toInt())
        buffer.p1Alt1(message.ownershipType)
        buffer.p2(message.timeUntilPublic)
        buffer.p4Alt3(message.quantity)
        buffer.p1(message.coordInZonePacked)
    }
}
