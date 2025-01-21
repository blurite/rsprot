package net.rsprot.protocol.game.outgoing.codec.zone.payload

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.common.game.outgoing.codec.zone.payload.ZoneProtEncoder
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.game.outgoing.zone.payload.LocAddChangeV2

public class LocAddChangeV2Encoder : ZoneProtEncoder<LocAddChangeV2> {
    override val prot: ServerProt = GameServerProt.LOC_ADD_CHANGE_V2

    override fun encode(
        buffer: JagByteBuf,
        message: LocAddChangeV2,
    ) {
        // The function at the bottom of the LOC_ADD_CHANGE has a consistent order,
        // making it easy to identify all the properties of this packet:
        // loc_add_change_del(world, level, x, z, layer, id, shape, rotation, opFlags, ops, 0, -1);
        buffer.p1(message.coordInZonePacked)
        buffer.p1Alt2(message.locPropertiesPacked)
        val ops = message.ops
        val opCount = ops?.size ?: 0
        buffer.p1Alt2(opCount)
        if (!ops.isNullOrEmpty()) {
            for ((key, value) in ops) {
                buffer.p1Alt1(key.toInt() + 1)
                buffer.pjstr(value)
            }
        }
        buffer.p1Alt2(message.opFlags.toInt())
        buffer.p2(message.id)
    }
}
