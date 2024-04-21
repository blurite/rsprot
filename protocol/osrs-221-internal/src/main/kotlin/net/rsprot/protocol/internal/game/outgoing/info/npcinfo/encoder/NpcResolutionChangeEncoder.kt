package net.rsprot.protocol.internal.game.outgoing.info.npcinfo.encoder

import net.rsprot.buffer.bitbuffer.BitBuf
import net.rsprot.protocol.internal.game.outgoing.info.CoordGrid
import net.rsprot.protocol.internal.game.outgoing.info.npcinfo.NpcAvatarDetails
import net.rsprot.protocol.shared.platform.PlatformType

public interface NpcResolutionChangeEncoder {
    public val platform: PlatformType

    public fun encode(
        bitBuffer: BitBuf,
        details: NpcAvatarDetails,
        extendedInfo: Boolean,
        localPlayerCoordGrid: CoordGrid,
        largeDistance: Boolean,
    )
}
