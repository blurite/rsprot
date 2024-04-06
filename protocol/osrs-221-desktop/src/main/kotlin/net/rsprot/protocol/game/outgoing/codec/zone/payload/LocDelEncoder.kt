package net.rsprot.protocol.game.outgoing.codec.zone.payload

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.game.outgoing.zone.payload.LocDel

public class LocDelEncoder : ZoneProtEncoder<LocDel> {
    override val prot: ServerProt = GameServerProt.LOC_DEL

    override fun encode(
        buffer: JagByteBuf,
        message: LocDel,
    ) {
        // The function at the bottom of the LOC_DEL has a consistent order,
        // making it easy to identify all the properties of this packet:
        // loc_add_change_del(level, x, z, layer, -1, shape, rotation, 31, 0, -1)
        buffer.p1Alt3(message.coordInZonePacked)
        buffer.p1Alt3(message.locPropertiesPacked)
    }
}
