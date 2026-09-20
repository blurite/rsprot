package net.rsprot.protocol.game.outgoing.codec.zone.payload

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import net.rsprot.protocol.game.outgoing.zone.payload.LocMerge
import net.rsprot.protocol.internal.game.outgoing.codec.zone.payload.ZoneProtEncoder

public class LocMergeEncoder : ZoneProtEncoder<LocMerge> {
    override val prot: ServerProt = GameServerProt.LOC_MERGE

    override fun encode(
        buffer: JagByteBuf,
        message: LocMerge,
    ) {
        // TODO(241): Native consumes all 14 bytes without applying a merge.
        // No active 241 field layout is verified; do not transmit the copied 240 payload.
        throw UnsupportedOperationException("LOC_MERGE has no verified active revision-241 layout")
    }
}
