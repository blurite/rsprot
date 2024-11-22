package net.rsprot.protocol.game.outgoing.info.playerinfo

import net.rsprot.buffer.bitbuffer.UnsafeLongBackedBitBuf
import net.rsprot.protocol.common.game.outgoing.info.CoordGrid
import net.rsprot.protocol.game.outgoing.info.playerinfo.PlayerInfoProtocol.Companion.PROTOCOL_CAPACITY
import net.rsprot.protocol.game.outgoing.info.playerinfo.util.CellOpcodes
import net.rsprot.protocol.game.outgoing.info.playerinfo.util.LowResolutionPosition
import kotlin.math.abs

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
     * An array of precalculated low resolution buffers. These must be stored off of player info objects,
     * as we need to still send them when a player logs out. If the buffer is null, there is no
     * low resolution update happening for that player.
     */
    private val buffers: Array<UnsafeLongBackedBitBuf?> = arrayOfNulls(PROTOCOL_CAPACITY)

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
     * Prepares the low resolution buffer for the player at index [idx].
     * @param idx the index of the player to prepare the low resolution buffer for.
     */
    internal fun prepareLowResBuffer(idx: Int) {
        this.buffers[idx] = calculateLowResolutionBuffer(idx)
    }

    /**
     * Calculates the low resolution buffer for player at index [idx], or null if there was no
     * change in their low resolution coordinate.
     * @param idx the index of the player to calculate for.
     * @return a bitpacked buffer containing the low resolution coordinate.
     */
    private fun calculateLowResolutionBuffer(idx: Int): UnsafeLongBackedBitBuf? {
        val old = getPreviousLowResolutionPosition(idx)
        val cur = getCurrentLowResolutionPosition(idx)
        if (old == cur) {
            return null
        }
        val buffer = UnsafeLongBackedBitBuf()
        val deltaX = cur.x - old.x
        val deltaZ = cur.z - old.z
        val deltaLevel = cur.level - old.level
        if (deltaX == 0 && deltaZ == 0) {
            buffer.pBits(2, 1)
            buffer.pBits(2, deltaLevel)
        } else if (abs(deltaX) <= 1 && abs(deltaZ) <= 1) {
            buffer.pBits(2, 2)
            buffer.pBits(2, deltaLevel)
            buffer.pBits(3, CellOpcodes.singleCellMovementOpcode(deltaX, deltaZ))
        } else {
            buffer.pBits(2, 3)
            buffer.pBits(2, deltaLevel)
            buffer.pBits(8, deltaX and 0xFF)
            buffer.pBits(8, deltaZ and 0xFF)
        }
        return buffer
    }

    /**
     * Gets the low resolution buffer for the player at index [idx], or null if there was
     * no low resolution update for that player.
     */
    internal fun getBuffer(idx: Int): UnsafeLongBackedBitBuf? = buffers[idx]

    /**
     * Gets the previous cycle's low resolution position of the player at index [index].
     * @param index the index of the player
     * @return the low resolution position of that player in the last cycle.
     */
    internal fun getPreviousLowResolutionPosition(index: Int): LowResolutionPosition =
        LowResolutionPosition(previousLowResPositions[index])

    /**
     * Gets the current cycle's low resolution position of the player at index [index].
     * @param index the index of the player
     * @return the low resolution position of that player in the current cycle.
     */
    internal fun getCurrentLowResolutionPosition(index: Int): LowResolutionPosition =
        LowResolutionPosition(currentLowResPositions[index])

    /**
     * Synchronize the low resolution positions at the end of the cycle.
     * This function will move all current positions over to the previous cycle.
     */
    internal fun postUpdate() {
        currentLowResPositions.copyInto(previousLowResPositions)
    }
}
