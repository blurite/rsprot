package net.rsprot.protocol.game.outgoing.info.npcinfo

import io.netty.buffer.ByteBuf
import net.rsprot.protocol.common.game.outgoing.info.CoordGrid
import net.rsprot.protocol.game.outgoing.info.ObserverExtendedInfoFlags
import net.rsprot.protocol.game.outgoing.info.util.BuildArea

/**
 * A world detail implementation for NPC info, tracking local NPCs in a specific world.
 * @property worldId the id of the world in which the NPCs exist.
 */
@ExperimentalUnsignedTypes
internal class NpcInfoWorldDetails(
    internal var worldId: Int,
) {
    /**
     * The last cycle's coordinate of the local player, used to perform faster npc removal.
     * If the player moves a greater distance than the [NpcInfo.viewDistance], we can make the assumption
     * that all the existing high-resolution NPCs need to be removed, and thus remove them
     * in a simplified manner, rather than applying a coordinate check on each one. This commonly
     * occurs whenever a player teleports far away.
     */
    internal var localPlayerLastCoord: CoordGrid = CoordGrid.INVALID

    /**
     * The current coordinate of the local player used for the calculations of this npc info
     * packet. This will be cross-referenced against NPCs to ensure they are within distance.
     */
    internal var localPlayerCurrentCoord: CoordGrid = CoordGrid.INVALID

    /**
     * The entire build area of this world - this effectively caps what we can see
     * to be within this block of land. Anything outside will be excluded.
     */
    internal var buildArea: BuildArea = BuildArea.INVALID

    /**
     * The indices of the high resolution NPCs, in the order as they came in.
     * This is a replica of how the client keeps track of NPCs.
     */
    internal var highResolutionNpcIndices: UShortArray =
        UShortArray(MAX_HIGH_RESOLUTION_NPCS) {
            NPC_INDEX_TERMINATOR
        }

    /**
     * A secondary array for high resolution NPCs.
     * After each cycle, the [highResolutionNpcIndices] gets swapped with this property,
     * and the indices will be appended one by one. As a result of it, we can get away
     * with significantly fewer operations to defragment the array, as we don't have to
     * shift every entry over, we only need to fill in the ones that still exist.
     */
    internal var temporaryHighResolutionNpcIndices: UShortArray =
        UShortArray(MAX_HIGH_RESOLUTION_NPCS) {
            NPC_INDEX_TERMINATOR
        }

    /**
     * A counter for how many high resolution NPCs are currently being tracked.
     * This count cannot exceed [MAX_HIGH_RESOLUTION_NPCS], as the client
     * only supports that many extended info updates.
     */
    internal var highResolutionNpcIndexCount: Int = 0

    /**
     * The extended info indices contain pointers to all the npcs for whom we need to
     * write an extended info block. We do this rather than directly writing them as this
     * improves CPU cache locality and allows us to batch extended info blocks together.
     */
    internal val extendedInfoIndices: UShortArray = UShortArray(MAX_HIGH_RESOLUTION_NPCS)

    /**
     * The number of npcs for whom we need to write extended info blocks this cycle.
     */
    internal var extendedInfoCount: Int = 0

    /**
     * The observer extended info flags are a means to track which extended info blocks
     * we need to transmit when moving a NPC from low resolution to high resolution,
     * as there are numerous extended info blocks which hold state over a long period
     * of time, such as head icon changes - if we didn't do this, anyone that observes
     * a NPC after the cycle during which the head icons were set, would not see these
     * head icons.
     */
    internal val observerExtendedInfoFlags: ObserverExtendedInfoFlags =
        ObserverExtendedInfoFlags(MAX_HIGH_RESOLUTION_NPCS)

    /**
     * The primary npc info buffer, holding all the bitcodes and extended info blocks.
     */
    internal var buffer: ByteBuf? = null

    /**
     * Whether the buffer allocated by this NPC info object has been built
     * into a packet message. If this returns false, but NPC info was in fact built,
     * we have an allocated buffer that needs releasing. If the NPC info itself
     * is released but isn't built into packet, we make sure to release it, to avoid
     * any memory leaks.
     */
    internal var builtIntoPacket: Boolean = false

    /**
     * Performs an index defragmentation on the [highResolutionNpcIndices] array.
     * This function will effectively take all indices that are NOT [NPC_INDEX_TERMINATOR]
     * and put them into the [temporaryHighResolutionNpcIndices] in a consecutive order,
     * without gaps. Afterwards, the [temporaryHighResolutionNpcIndices] and [highResolutionNpcIndices]
     * arrays get swapped out, so our [highResolutionNpcIndices] becomes a defragmented array.
     * This process occurs every cycle, after high resolution indices are processed, in order to
     * get rid of any gaps that were produced as a result of it.
     *
     * A breakdown of this process:
     * At the start of a cycle, we might have indices as `[1, 7, 5, 3, 8, 65535, ...]`
     * If we make the assumption that NPCs at indices 7 and 8 are being removed from our high resolution,
     * during the high resolution processing, npc at index 8 is dropped naturally - this is because
     * the client will automatically trim off any NPCs at the end which don't fit into the transmitted
     * count. So, npc at index 8 does not count towards fragmentation, as we just decrement the index count.
     * However, index 7, because it is in the middle of this array of indices, causes the array
     * to fragment. So in order to resolve this, we will iterate the fragmented indices
     * until we have collected [highResolutionNpcIndexCount] worth of valid indices into the
     * [temporaryHighResolutionNpcIndices] array.
     * After defragmenting, our array will look as `[1, 5, 3, 65535, ...]`.
     * While it is possible to do this with a single array, it requires one to shift every element
     * in the array after the first fragmentation occurs. As the arrays are relatively small, it's
     * better simply to use two arrays that get swapped every cycle, so we simply swap
     * the [temporaryHighResolutionNpcIndices] and [highResolutionNpcIndices] arrays between one another,
     * rather than needing to shift everything over.
     */
    internal fun defragmentIndices() {
        var count = 0
        for (i in highResolutionNpcIndices.indices) {
            if (count >= highResolutionNpcIndexCount) {
                break
            }
            val index = highResolutionNpcIndices[i]
            if (index != NPC_INDEX_TERMINATOR) {
                temporaryHighResolutionNpcIndices[count++] = index
            }
        }
        val uncompressed = this.highResolutionNpcIndices
        this.highResolutionNpcIndices = this.temporaryHighResolutionNpcIndices
        this.temporaryHighResolutionNpcIndices = uncompressed
    }

    /**
     * Resets all the properties of this world details implementation, allowing
     * it to be re-used for another player.
     * @param worldId the new world id to be used for these details.
     */
    internal fun onAlloc(worldId: Int) {
        this.worldId = worldId
        this.localPlayerCurrentCoord = CoordGrid.INVALID
        this.localPlayerLastCoord = localPlayerCurrentCoord
        this.buildArea = BuildArea.INVALID
        this.highResolutionNpcIndexCount = 0
        this.highResolutionNpcIndices.fill(0u)
        this.temporaryHighResolutionNpcIndices.fill(0u)
        this.extendedInfoCount = 0
        this.extendedInfoIndices.fill(0u)
        this.observerExtendedInfoFlags.reset()
        val buffer = this.buffer
        if (buffer != null) {
            if (!builtIntoPacket) {
                buffer.release(buffer.refCnt())
            }
            this.buffer = null
        }
        this.builtIntoPacket = false
    }

    private companion object {
        /**
         * The maximum number of high resolution NPCs that the client supports, limited by the
         * client's array of extended info updates being a size-250 int array.
         */
        private const val MAX_HIGH_RESOLUTION_NPCS: Int = 250

        /**
         * The terminator value used to indicate that no NPC is here.
         */
        private const val NPC_INDEX_TERMINATOR: UShort = 0xFFFFu
    }
}
