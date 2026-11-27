package net.rsprot.protocol.game.outgoing.codec.zone.payload

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.game.outgoing.zone.payload.LocAnim
import net.rsprot.protocol.internal.game.outgoing.codec.zone.payload.ZoneProtEncoder

public class LocAnimEncoder : ZoneProtEncoder<LocAnim> {
    override val prot: ServerProt = GameServerProt.LOC_ANIM

    override fun encode(
        buffer: JagByteBuf,
        message: LocAnim,
    ) {
        buffer.p1Alt2(message.coordInZonePacked)
        buffer.p1Alt1(message.locPropertiesPacked)
        buffer.p2Alt3(message.id)
    }
}
