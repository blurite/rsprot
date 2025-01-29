package net.rsprot.protocol.internal.game.outgoing.info.npcinfo.encoder

import net.rsprot.buffer.bitbuffer.BitBuf
import net.rsprot.protocol.common.client.OldSchoolClientType
import net.rsprot.protocol.internal.game.outgoing.info.CoordGrid
import net.rsprot.protocol.internal.game.outgoing.info.npcinfo.NpcAvatarDetails

public interface NpcResolutionChangeEncoder {
    public val clientType: OldSchoolClientType

    public fun encode(
	    bitBuffer: BitBuf,
	    details: net.rsprot.protocol.internal.game.outgoing.info.npcinfo.NpcAvatarDetails,
	    extendedInfo: Boolean,
	    localPlayerCoordGrid: net.rsprot.protocol.internal.game.outgoing.info.CoordGrid,
	    largeDistance: Boolean,
	    cycleCount: Int,
    )
}
