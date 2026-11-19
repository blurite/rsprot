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
        buffer.p1Alt2(message.opFlags.toInt())
        buffer.p2(message.id)
        buffer.p1Alt1(message.coordInZonePacked)
    }
}
