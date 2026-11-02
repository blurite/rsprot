package net.rsprot.protocol.game.outgoing.codec.zone.payload

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.game.outgoing.zone.payload.LocAddChangeV3
import net.rsprot.protocol.internal.game.outgoing.codec.zone.payload.ZoneProtEncoder

public class LocAddChangeV3Encoder : ZoneProtEncoder<LocAddChangeV3> {
    override val prot: ServerProt = GameServerProt.LOC_ADD_CHANGE_V3

    override fun encode(
        buffer: JagByteBuf,
        message: LocAddChangeV3,
    ) {
        buffer.p1Alt2(message.coordInZonePacked)
        buffer.p1Alt2(message.opFlags.toInt())
        val ops = message.ops
        val opCount = ops?.size ?: 0
        buffer.p1(opCount)
        if (!ops.isNullOrEmpty()) {
            for ((key, value) in ops) {
                buffer.p1(key.toInt() - 1)
                buffer.pjstr(value)
            }
        }
        buffer.p3Alt2(message.id)
        buffer.p1Alt3(message.locPropertiesPacked)
    }
}
