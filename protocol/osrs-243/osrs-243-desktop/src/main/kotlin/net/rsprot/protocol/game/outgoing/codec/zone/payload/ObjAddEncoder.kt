package net.rsprot.protocol.game.outgoing.codec.zone.payload

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.game.outgoing.zone.payload.ObjAdd
import net.rsprot.protocol.internal.game.outgoing.codec.zone.payload.ZoneProtEncoder

public class ObjAddEncoder : ZoneProtEncoder<ObjAdd> {
    override val prot: ServerProt = GameServerProt.OBJ_ADD

    override fun encode(
        buffer: JagByteBuf,
        message: ObjAdd,
    ) {
        buffer.p2Alt1(message.id)
        buffer.p1Alt3(message.ownershipType)
        buffer.p1Alt1(message.coordInZonePacked)
        buffer.p2(message.timeUntilDespawn)
        buffer.p2(message.timeUntilPublic)
        buffer.p1Alt1(message.opFlags.toInt())
        buffer.p1(if (message.neverBecomesPublic) 1 else 0)
        buffer.p4Alt1(message.quantity)
    }
}
