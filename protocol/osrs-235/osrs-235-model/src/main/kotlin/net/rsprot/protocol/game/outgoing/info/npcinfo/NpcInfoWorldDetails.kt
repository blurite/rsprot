package net.rsprot.protocol.game.outgoing.info.npcinfo

import io.netty.buffer.ByteBuf
import net.rsprot.protocol.game.outgoing.info.ObserverExtendedInfoFlags

/**
 * A world detail implementation for NPC info, tracking local NPCs in a specific world.
 * @property worldId the id of the world in which the NPCs exist.
 */
@OptIn(ExperimentalUnsignedTypes::class)
internal class NpcInfoWorldDetails(
    internal var worldId: Int,
) {
    /**
     * The maximum number of NPCs that can render at once in the
     * [net.rsprot.protocol.game.outgoing.info.AvatarPriority.LOW] priority group.
     */
    internal var lowPriorityCap: Int = MAX_HIGH_RESOLUTION_NPCS

    /**
     * The current number of NPCs occupying the low priority group.
     */
    internal var lowPriorityCount: Int = 0

    /**
     * The maximum number of NPCs that can render at once in the
     * [net.rsprot.protocol.game.outgoing.info.AvatarPriority.NORMAL] priority group.
     * Note that any normal priority NPC will be able to make use of [lowPriorityCap] if the
     * normal priority soft cap has no more free slots.
     */
    internal var normalPrioritySoftCap: Int = 0

    /**
     * The current number of NPCs occupying the normal priority group.
     */
    internal var normalPriorityCount: Int = 0

    /**
     * Priority flags for currently tracked NPCs, one bit per possible high resolution slot.
     * A flag of 0x1 implies low priority; if the flag isn't set, that NPC is in normal priority.
     */
    private var highResolutionPriorityFlags = LongArray(3)

    /**
     * A secondary array of high resolution priority flags.
     * This does the same as [highResolutionPriorityFlags], but because a defragmentation process
     * can take place, we need to re-sort the values.
     */
    private var temporaryHighResolutionPriorityFlags = LongArray(3)

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
    private var temporaryHighResolutionNpcIndices: UShortArray =
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
     * The number of high resolution NPCs in the last cycle.
     */
    internal var lastCycleHighResolutionNpcIndexCount: Int = 0

    /**
     * The previous npc info packet that was created.
     * We ensure that a server hasn't accidentally left a packet unwritten, which would
     * de-synchronize the client and cause errors.
     */
    internal var previousPacket: NpcInfoPacket? = null

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
                temporaryHighResolutionNpcIndices[count] = index
                if (isLowPriority(i)) {
                    setLowPriority(count, temporaryHighResolutionPriorityFlags)
                }
                count++
            }
        }
        val uncompressed = this.highResolutionNpcIndices
        this.highResolutionNpcIndices = this.temporaryHighResolutionNpcIndices
        this.temporaryHighResolutionNpcIndices = uncompressed

        val priorities = this.highResolutionPriorityFlags
        this.highResolutionPriorityFlags = this.temporaryHighResolutionPriorityFlags
        this.temporaryHighResolutionPriorityFlags = priorities
        this.temporaryHighResolutionPriorityFlags.fill(0L)
    }

    /**
     * Decrements the priority counters for the given high resolution slot [index].
     * @param index the high resolution (not absolute) index of the NPC, a value from 0 to 149.
     */
    internal fun decrementPriority(index: Int) {
        if (isLowPriority(index)) {
            unsetLowPriority(index)
            if (lowPriorityCount > 0) {
                --lowPriorityCount
            }
        } else {
            if (normalPriorityCount > 0) {
                --normalPriorityCount
            }
        }
    }

    /**
     * Increments the priority for a given slot [index].
     * If the NPC is not low priority, but our normal priority group is full, we instead
     * assign that NPC to the low priority group.
     * @param index the high resolution (not absolute) index of the NPC, a value from 0 to 149.
     * @param isLowPriority whether the NPC belongs in the low priority category.
     */
    internal fun incrementPriority(
        index: Int,
        isLowPriority: Boolean,
    ) {
        if (isLowPriority || (normalPriorityCount >= normalPrioritySoftCap)) {
            setLowPriority(index)
            lowPriorityCount++
        } else {
            normalPriorityCount++
        }
    }

    /**
     * Checks whether the npc at high resolution slot [index] is in the low priority group.
     * @param index the high resolution (not absolute) index of the NPC, a value from 0 to 149.
     * @param array the array from which to check whether a bit is set.
     */
    private fun isLowPriority(
        index: Int,
        array: LongArray = this.highResolutionPriorityFlags,
    ): Boolean {
        val longIndex = index ushr 6
        val bit = 1L shl (index and 0x3F)
        return array[longIndex] and bit != 0L
    }

    /**
     * Marks the npc at high resolution slot [index] as low priority.
     * @param index the high resolution (not absolute) index of the NPC, a value from 0 to 149.
     * @param array the array in which to modify the corresponding bit.
     */
    internal fun setLowPriority(
        index: Int,
        array: LongArray = this.highResolutionPriorityFlags,
    ) {
        val longIndex = index ushr 6
        val bit = 1L shl (index and 0x3F)
        val cur = array[longIndex]
        array[longIndex] = cur or bit
    }

    /**
     * Unmarks the npc at high resolution slot [index] as low priority.
     * @param index the high resolution (not absolute) index of the NPC, a value from 0 to 149.
     * @param array the array in which to modify the corresponding bit.
     */
    internal fun unsetLowPriority(
        index: Int,
        array: LongArray = this.highResolutionPriorityFlags,
    ) {
        val longIndex = index ushr 6
        val bit = 1L shl (index and 0x3F)
        val cur = array[longIndex]
        array[longIndex] = cur and bit.inv()
    }

    /**
     * Clears any priority flags currently set and resets the priority counts to zero.
     */
    internal fun clearPriorities() {
        this.highResolutionPriorityFlags.fill(0L)
        this.temporaryHighResolutionPriorityFlags.fill(0L)
        this.lowPriorityCount = 0
        this.normalPriorityCount = 0
    }

    /**
     * Resets all the properties of this world details implementation, allowing
     * it to be re-used for another player.
     * @param worldId the new world id to be used for these details.
     */
    internal fun onAlloc(worldId: Int) {
        this.worldId = worldId
        this.highResolutionNpcIndexCount = 0
        this.highResolutionNpcIndices.fill(0u)
        this.temporaryHighResolutionNpcIndices.fill(0u)
        this.extendedInfoCount = 0
        this.extendedInfoIndices.fill(0u)
        this.observerExtendedInfoFlags.reset()
        this.highResolutionPriorityFlags.fill(0L)
        this.temporaryHighResolutionPriorityFlags.fill(0L)
        this.lowPriorityCap = MAX_HIGH_RESOLUTION_NPCS
        this.normalPrioritySoftCap = 0
        this.lowPriorityCount = 0
        this.normalPriorityCount = 0
        this.buffer = null
        this.previousPacket = null
        this.lastCycleHighResolutionNpcIndexCount = 0
    }

    internal fun onDealloc() {
        this.buffer = null
        this.previousPacket = null
    }

    private companion object {
        /**
         * The maximum number of high resolution NPCs that the client supports, limited by the
         * client's array of extended info updates being a size-149 int array.
         * Starting with revision 229, on Java clients, the limit is 149, and OSRS
         * does indeed only send 149 at most - tested via toy cats. On native, the limit
         * is still 250.
         */
        private const val MAX_HIGH_RESOLUTION_NPCS: Int = 149

        /**
         * The terminator value used to indicate that no NPC is here.
         */
        private const val NPC_INDEX_TERMINATOR: UShort = 0xFFFFu
    }
}
