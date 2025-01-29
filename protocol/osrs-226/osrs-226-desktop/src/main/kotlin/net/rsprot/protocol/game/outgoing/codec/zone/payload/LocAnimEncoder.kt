package net.rsprot.protocol.game.outgoing.codec.zone.payload

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.internal.game.outgoing.codec.zone.payload.ZoneProtEncoder
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.game.outgoing.zone.payload.LocAnim

public class LocAnimEncoder : ZoneProtEncoder<LocAnim> {
    override val prot: ServerProt = GameServerProt.LOC_ANIM

    override fun encode(
        buffer: JagByteBuf,
        message: LocAnim,
    ) {
        // The function
        // at the bottom of the LOC_ANIM has a consistent order,
        // making it easy to identify all the properties of this packet:
        // loc_anim(level, x, z, shape, rotation, layer, id)
        buffer.p1(message.locPropertiesPacked)
        buffer.p2Alt3(message.id)
        buffer.p1(message.coordInZonePacked)
    }
}
