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
        buffer.p4Alt2(message.quantity)
        buffer.p2Alt1(message.retexIndex)
        buffer.p2(message.model)
        buffer.p2Alt2(message.recol)
        buffer.p2Alt2(message.retex)
        buffer.p2Alt2(message.recolIndex)
        buffer.p1Alt2(message.coordInZonePacked)
        buffer.p2(message.id)
    }
}
