package net.rsprot.protocol.game.outgoing.codec.zone.payload

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.common.game.outgoing.codec.zone.payload.ZoneProtEncoder
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.game.outgoing.zone.payload.LocAddChange

public class LocAddChangeEncoder : ZoneProtEncoder<LocAddChange> {
    override val prot: ServerProt = GameServerProt.LOC_ADD_CHANGE

    override fun encode(
        buffer: JagByteBuf,
        message: LocAddChange,
    ) {
        // The function at the bottom of the LOC_ADD_CHANGE has a consistent order,
        // making it easy to identify all the properties of this packet:
        // loc_add_change_del(level, x, z, layer, id, shape, rotation, opFlags, 0, -1)
        buffer.p2Alt2(message.id)
        buffer.p1Alt1(message.coordInZonePacked)
        buffer.p1Alt3(message.locPropertiesPacked)
        buffer.p1Alt3(message.opFlags.toInt())
    }
}
