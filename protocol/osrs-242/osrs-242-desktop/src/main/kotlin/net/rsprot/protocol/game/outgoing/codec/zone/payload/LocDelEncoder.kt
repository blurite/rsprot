package net.rsprot.protocol.game.outgoing.codec.zone.payload

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.game.outgoing.zone.payload.LocDel
import net.rsprot.protocol.internal.game.outgoing.codec.zone.payload.ZoneProtEncoder

public class LocDelEncoder : ZoneProtEncoder<LocDel> {
    override val prot: ServerProt = GameServerProt.LOC_DEL

    override fun encode(
        buffer: JagByteBuf,
        message: LocDel,
    ) {
        buffer.p1Alt2(message.coordInZonePacked)
        buffer.p1Alt2(message.locPropertiesPacked)
    }
}
