package net.rsprot.protocol.game.outgoing.codec.zone.payload

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.game.outgoing.zone.payload.LocAddChangeV2
import net.rsprot.protocol.internal.game.outgoing.codec.zone.payload.ZoneProtEncoder

public class LocAddChangeV2Encoder : ZoneProtEncoder<LocAddChangeV2> {
    override val prot: ServerProt = GameServerProt.LOC_ADD_CHANGE_V2

    override fun encode(
        buffer: JagByteBuf,
        message: LocAddChangeV2,
    ) {
        buffer.p1Alt3(message.opFlags.toInt())
        val ops = message.ops
        val opCount = ops?.size ?: 0
        buffer.p1Alt1(opCount)
        if (!ops.isNullOrEmpty()) {
            for ((key, value) in ops) {
                buffer.p1Alt1(key.toInt() - 1)
                buffer.pjstr(value)
            }
        }
        buffer.p2Alt1(message.id)
        buffer.p1(message.coordInZonePacked)
        buffer.p1(message.locPropertiesPacked)
    }
}
