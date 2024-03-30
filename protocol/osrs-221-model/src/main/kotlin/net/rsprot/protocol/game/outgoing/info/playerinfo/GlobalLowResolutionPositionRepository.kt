package net.rsprot.protocol.game.outgoing.info.playerinfo

import net.rsprot.protocol.game.outgoing.info.playerinfo.PlayerInfoProtocol.Companion.PROTOCOL_CAPACITY
import net.rsprot.protocol.game.outgoing.info.playerinfo.util.LowResolutionPosition
import net.rsprot.protocol.internal.game.outgoing.info.CoordGrid

internal class GlobalLowResolutionPositionRepository() {
    private val previousLowResPositions: IntArray = IntArray(PROTOCOL_CAPACITY)
    private val currentLowResPositions: IntArray = IntArray(PROTOCOL_CAPACITY)

    internal fun update(
        idx: Int,
        coordGrid: CoordGrid,
    ) {
        val lowResolutionPosition = LowResolutionPosition(coordGrid)
        currentLowResPositions[idx] = lowResolutionPosition.packed
    }

    internal fun markUnused(idx: Int) {
        currentLowResPositions[idx] = 0
    }

    internal fun getPreviousLowResolutionPosition(index: Int): LowResolutionPosition {
        return LowResolutionPosition(previousLowResPositions[index])
    }

    internal fun getCurrentLowResolutionPosition(index: Int): LowResolutionPosition {
        return LowResolutionPosition(currentLowResPositions[index])
    }

    internal fun postUpdate() {
        currentLowResPositions.copyInto(previousLowResPositions)
    }
}
