package net.rsprot.protocol.game.outgoing.info.playerinfo

import net.rsprot.protocol.game.outgoing.info.playerinfo.PlayerInfoProtocol.Companion.PROTOCOL_CAPACITY
import net.rsprot.protocol.game.outgoing.info.playerinfo.util.LowResolutionPosition
import net.rsprot.protocol.internal.game.outgoing.info.CoordGrid

/**
 * A repository used to track the low resolution positions of all the player avatars in the world.
 * These low resolution positions will be synchronized to all the players, as part of the protocol.
 * As all observers receive the same information, rather than allocating this per-player basis,
 * we do it once, globally, for the entire world.
 */
internal class GlobalLowResolutionPositionRepository {
    /**
     * The low resolution positions of all the players in the previous cycle.
     */
    private val previousLowResPositions: IntArray = IntArray(PROTOCOL_CAPACITY)

    /**
     * The low resolution positions of all the players in the current cycle.
     */
    private val currentLowResPositions: IntArray = IntArray(PROTOCOL_CAPACITY)

    /**
     * Updates the current low resolution position of the player at index [idx].
     * @param idx the index of the player whose low resolution position to update.
     * @param coordGrid the new absolute coordinate of that player. The low resolution
     * coordinate will be calculated out of it.
     */
    internal fun update(
        idx: Int,
        coordGrid: CoordGrid,
    ) {
        val lowResolutionPosition = LowResolutionPosition(coordGrid)
        currentLowResPositions[idx] = lowResolutionPosition.packed
    }

    /**
     * Marks the player at index [idx] as unused. This should be done whenever a player logs out.
     * @param idx the index of the player.
     */
    internal fun markUnused(idx: Int) {
        currentLowResPositions[idx] = 0
    }

    /**
     * Gets the previous cycle's low resolution position of the player at index [index].
     * @param index the index of the player
     * @return the low resolution position of that player in the last cycle.
     */
    internal fun getPreviousLowResolutionPosition(index: Int): LowResolutionPosition {
        return LowResolutionPosition(previousLowResPositions[index])
    }

    /**
     * Gets the current cycle's low resolution position of the player at index [index].
     * @param index the index of the player
     * @return the low resolution position of that player in the current cycle.
     */
    internal fun getCurrentLowResolutionPosition(index: Int): LowResolutionPosition {
        return LowResolutionPosition(currentLowResPositions[index])
    }

    /**
     * Synchronize the low resolution positions at the end of the cycle.
     * This function will move all current positions over to the previous cycle.
     */
    internal fun postUpdate() {
        currentLowResPositions.copyInto(previousLowResPositions)
    }
}
