package net.rsprot.protocol.game.outgoing.codec.zone.payload

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.common.game.outgoing.codec.zone.payload.ZoneProtEncoder
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.game.outgoing.zone.payload.LocAddChangeV1

public class LocAddChangeV1Encoder : ZoneProtEncoder<LocAddChangeV1> {
    override val prot: ServerProt = GameServerProt.LOC_ADD_CHANGE_V1

    override fun encode(
        buffer: JagByteBuf,
        message: LocAddChangeV1,
    ) {
        // The function at the bottom of the LOC_ADD_CHANGE has a consistent order,
        // making it easy to identify all the properties of this packet:
        // loc_add_change_del(world, level, x, z, layer, id, shape, rotation, opFlags, null, 0, -1);
        buffer.p1Alt1(message.coordInZonePacked)
        buffer.p1Alt2(message.opFlags.toInt())
        buffer.p2Alt2(message.id)
        buffer.p1Alt2(message.locPropertiesPacked)
    }
}
