package net.rsprot.protocol.game.outgoing.codec.npcinfo

import net.rsprot.buffer.bitbuffer.BitBuf
import net.rsprot.protocol.common.client.OldSchoolClientType
import net.rsprot.protocol.common.game.outgoing.info.CoordGrid
import net.rsprot.protocol.common.game.outgoing.info.npcinfo.NpcAvatarDetails
import net.rsprot.protocol.common.game.outgoing.info.npcinfo.encoder.NpcResolutionChangeEncoder

public class DesktopLowResolutionChangeEncoder : NpcResolutionChangeEncoder {
    override val clientType: OldSchoolClientType = OldSchoolClientType.DESKTOP

    override fun encode(
        bitBuffer: BitBuf,
        details: NpcAvatarDetails,
        extendedInfo: Boolean,
        localPlayerCoordGrid: CoordGrid,
        largeDistance: Boolean,
    ) {
        val numOfBitsUsed = if (largeDistance) 8 else 5
        val maximumDistanceTransmittableByBits = if (largeDistance) 0xFF else 0x1F
        val deltaX = details.currentCoord.x - localPlayerCoordGrid.x
        val deltaZ = details.currentCoord.z - localPlayerCoordGrid.z
        bitBuffer.pBits(16, details.index)
        bitBuffer.pBits(14, details.id)
        bitBuffer.pBits(1, if (details.isJumping()) 1 else 0)
        bitBuffer.pBits(1, if (extendedInfo) 1 else 0)
        bitBuffer.pBits(numOfBitsUsed, deltaX and maximumDistanceTransmittableByBits)
        bitBuffer.pBits(3, details.direction)
        if (details.spawnCycle != 0) {
            bitBuffer.pBits(1, 1)
            bitBuffer.pBits(32, details.spawnCycle)
        } else {
            bitBuffer.pBits(1, 0)
        }
        bitBuffer.pBits(numOfBitsUsed, deltaZ and maximumDistanceTransmittableByBits)
    }
}
