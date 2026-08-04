package net.rsprot.protocol.game.outgoing.codec.npcinfo

import net.rsprot.buffer.bitbuffer.BitBuf
import net.rsprot.protocol.common.client.OldSchoolClientType
import net.rsprot.protocol.internal.game.outgoing.info.CoordGrid
import net.rsprot.protocol.internal.game.outgoing.info.npcinfo.NpcAvatarDetails
import net.rsprot.protocol.internal.game.outgoing.info.npcinfo.encoder.NpcResolutionChangeEncoder

public class DesktopLowResolutionChangeEncoder : NpcResolutionChangeEncoder {
    override val clientType: OldSchoolClientType = OldSchoolClientType.DESKTOP

    override fun encode(
        bitBuffer: BitBuf,
        details: NpcAvatarDetails,
        extendedInfo: Boolean,
        localPlayerCoordGrid: CoordGrid,
        largeDistance: Boolean,
        cycleCount: Int,
    ) {
        val numOfBitsUsed = if (largeDistance) 8 else 6
        val maximumDistanceTransmittableByBits = if (largeDistance) 0xFF else 0x3F
        val deltaX = details.currentCoord.x - localPlayerCoordGrid.x
        val deltaZ = details.currentCoord.z - localPlayerCoordGrid.z

        bitBuffer.pBits(16, details.index)

        if (details.spawnCycle != 0) {
            bitBuffer.pBits(1, 1)
            val cycle = details.spawnCycle
            val index =
                if (cycle < 0) {
                    spawnClockMaxValues.lastIndex
                } else {
                    spawnClockMaxValues.indexOfFirst { cycle <= it }
                }
            bitBuffer.pBits(2, index)
            bitBuffer.pBits(spawnClockBitcodes[index], cycle)
        } else {
            bitBuffer.pBits(1, 0)
        }

        val type = details.id
        val typeIndex = typeMaxValues.indexOfFirst { type <= it }
        bitBuffer.pBits(2, typeIndex)
        bitBuffer.pBits(typeBitcodes[typeIndex], type)

        bitBuffer.pBits(1, if (extendedInfo) 1 else 0)
        bitBuffer.pBits(numOfBitsUsed, deltaX and maximumDistanceTransmittableByBits)
        bitBuffer.pBits(3, details.direction)
        // New NPCs should always be marked as "jumping" unless they explicitly only teleported without a jump
        val noJump = details.isTeleWithoutJump() && details.allocateCycle != cycleCount
        bitBuffer.pBits(1, if (noJump) 0 else 1)
        bitBuffer.pBits(numOfBitsUsed, deltaZ and maximumDistanceTransmittableByBits)
    }

    private companion object {
        private val spawnClockBitcodes = intArrayOf(18, 19, 20, 32)
        private val spawnClockMaxValues = spawnClockBitcodes.map { (1 shl it) - 1 }.toIntArray()
        private val typeBitcodes = intArrayOf(12, 14, 17, 24)
        private val typeMaxValues = typeBitcodes.map { (1 shl it) - 1 }.toIntArray()
    }
}
