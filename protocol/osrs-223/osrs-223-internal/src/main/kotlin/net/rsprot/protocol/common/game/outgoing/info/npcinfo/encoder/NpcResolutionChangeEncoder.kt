package net.rsprot.protocol.common.game.outgoing.info.npcinfo.encoder

import net.rsprot.buffer.bitbuffer.BitBuf
import net.rsprot.protocol.common.client.OldSchoolClientType
import net.rsprot.protocol.common.game.outgoing.info.CoordGrid
import net.rsprot.protocol.common.game.outgoing.info.npcinfo.NpcAvatarDetails

public interface NpcResolutionChangeEncoder {
    public val clientType: OldSchoolClientType

    public fun encode(
        bitBuffer: BitBuf,
        details: NpcAvatarDetails,
        extendedInfo: Boolean,
        localPlayerCoordGrid: CoordGrid,
        largeDistance: Boolean,
        cycleCount: Int,
    )
}
