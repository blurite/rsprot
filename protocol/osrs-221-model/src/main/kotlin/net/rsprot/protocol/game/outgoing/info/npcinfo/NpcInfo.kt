package net.rsprot.protocol.game.outgoing.info.npcinfo

import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufAllocator
import net.rsprot.buffer.bitbuffer.BitBuf
import net.rsprot.buffer.bitbuffer.toBitBuf
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprot.protocol.common.game.outgoing.info.CoordGrid
import net.rsprot.protocol.common.game.outgoing.info.npcinfo.encoder.NpcResolutionChangeEncoder
import net.rsprot.protocol.common.platform.PlatformMap
import net.rsprot.protocol.common.platform.PlatformType
import net.rsprot.protocol.game.outgoing.info.ObserverExtendedInfoFlags
import net.rsprot.protocol.game.outgoing.info.util.ReferencePooledObject
import net.rsprot.protocol.message.OutgoingGameMessage
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

/**
 * An implementation of the npc info packet.
 * This class is responsible for bringing together all the bits of the npc info protocol,
 * including copying all the pre-built buffers that were made beforehand.
 * @property allocator the byte buffer allocator used to allocate new buffers to be used
 * for the npc info packet, as well as the pre-built extended info buffers.
 * @property repository the npc avatar repository, keeping track of every npc avatar that exists
 * in the game.
 * @property platformType the platform the player owning this npc info packet is on
 * @property localPlayerIndex the index of the local player that owns this npc info packet.
 * @property indexSupplier a supplier-style interface responsible for yielding npc indices
 * which are within vicinity of the player. This is the primary way the server will be providing
 * information about nearby NPCs to a player, as well as whether to render the NPC in the first place,
 * as some NPCs are meant to only render to a given player if certain conditions are met.
 * @property lowResolutionToHighResolutionEncoders a platform map of low resolution to high resolution
 * change encoders, used to move a npc into high resolution for the given player.
 * As this is scrambled, a separate platform-specific implementation is required.
 */
@Suppress("ReplaceUntilWithRangeUntil")
@ExperimentalUnsignedTypes
public class NpcInfo internal constructor(
    private val allocator: ByteBufAllocator,
    private val repository: NpcAvatarRepository,
    private var platformType: PlatformType,
    private var localPlayerIndex: Int,
    private val indexSupplier: NpcIndexSupplier,
    private val lowResolutionToHighResolutionEncoders: PlatformMap<NpcResolutionChangeEncoder>,
) : ReferencePooledObject, OutgoingGameMessage {
    /**
     * The last cycle's coordinate of the local player, used to perform faster npc removal.
     * If the player moves a greater distance than the [viewDistance], we can make the assumption
     * that all the existing high-resolution NPCs need to be removed, and thus remove them
     * in a simplified manner, rather than applying a coordinate check on each one. This commonly
     * occurs whenever a player teleports far away.
     */
    private var localPlayerLastCoord: CoordGrid = CoordGrid.INVALID

    /**
     * The current coordinate of the local player used for the calculations of this npc info
     * packet. This will be cross-referenced against NPCs to ensure they are within distance.
     */
    private var localPlayerCurrentCoord: CoordGrid = CoordGrid.INVALID

    /**
     * The maximum view distance how far a player will see other NPCs.
     * Unlike with player info, this does not automatically resize to accommodate for nearby NPCs,
     * as it is almost impossible for such a scenario to happen in the first place.
     * It is confirmed that OldSchool RuneScape does not do it either.
     */
    private var viewDistance: Int = MAX_SMALL_PACKET_DISTANCE

    /**
     * The indices of the high resolution NPCs, in the order as they came in.
     * This is a replica of how the client keeps track of NPCs.
     */
    private var highResolutionNpcIndices: UShortArray =
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
    private var highResolutionNpcIndexCount: Int = 0

    /**
     * The extended info indices contain pointers to all the npcs for whom we need to
     * write an extended info block. We do this rather than directly writing them as this
     * improves CPU cache locality and allows us to batch extended info blocks together.
     */
    private val extendedInfoIndices: UShortArray = UShortArray(MAX_HIGH_RESOLUTION_NPCS)

    /**
     * The number of npcs for whom we need to write extended info blocks this cycle.
     */
    private var extendedInfoCount: Int = 0

    /**
     * The observer extended info flags are a means to track which extended info blocks
     * we need to transmit when moving a NPC from low resolution to high resolution,
     * as there are numerous extended info blocks which hold state over a long period
     * of time, such as head icon changes - if we didn't do this, anyone that observes
     * a NPC after the cycle during which the head icons were set, would not see these
     * head icons.
     */
    private val observerExtendedInfoFlags: ObserverExtendedInfoFlags =
        ObserverExtendedInfoFlags(MAX_HIGH_RESOLUTION_NPCS)

    /**
     * The primary npc info buffer, holding all the bitcodes and extended info blocks.
     */
    private var buffer: ByteBuf? = null

    /**
     * Returns the backing byte buffer holding all the computed information.
     * @throws IllegalStateException if the buffer is null, meaning it has no yet been
     * initialized for this cycle.
     */
    @Throws(IllegalStateException::class)
    public fun backingBuffer(): ByteBuf {
        return checkNotNull(buffer)
    }

    /**
     * Allocates a new buffer from the [allocator] with a capacity of [BUF_CAPACITY].
     * The old [buffer] will not be released, as that is the duty of the encoder class.
     */
    private fun allocBuffer(): ByteBuf {
        // Acquire a new buffer with each cycle, in case the previous one isn't fully written out yet
        val buffer = allocator.buffer(BUF_CAPACITY, BUF_CAPACITY)
        this.buffer = buffer
        return buffer
    }

    /**
     * Updates the coordinate of the local player, as this is necessary to know
     * how far NPCs nearby are to the player, which allows us to remove NPCs that
     * have gone too far out, and add NPCs that are within certain distance.
     * @param level the height level of the local player
     * @param x the x coordinate of the local player
     * @param z the z coordinate of the local player
     */
    public fun updateCoord(
        level: Int,
        x: Int,
        z: Int,
    ) {
        this.localPlayerCurrentCoord =
            CoordGrid(
                level,
                x,
                z,
            )
    }

    /**
     * Computes the high resolution and low resolution bitcodes for this given player,
     * additionally marks down which NPCs need to furthermore send their extended info
     * updates.
     */
    public fun compute() {
        val viewDistance = this.viewDistance
        val buffer = allocBuffer()
        buffer.toBitBuf().use { bitBuffer ->
            val fragmented = processHighResolution(bitBuffer, viewDistance)
            if (fragmented) {
                defragmentIndices()
            }
            processLowResolution(bitBuffer, viewDistance)
            // Terminate the low-resolution processing block if there are extended info
            // blocks after that; if not, the loop ends naturally due to not enough
            // readable bits remaining (at most would have 7 bits remaining due to
            // the bit writer closing, which "finishes" the current byte).
            if (this.extendedInfoCount > 0) {
                bitBuffer.pBits(16, 0xFFFF)
            }
        }
    }

    /**
     * Synchronizes the last coordinate of the local player with the current coordinate
     * set previously in this cycle. This is simply to help make removal of all NPCs
     * in high resolution more efficient, as we can avoid distance checks against every
     * NPC, and only do so against the player's last coordinate.
     */
    public fun afterUpdate() {
        this.localPlayerLastCoord = localPlayerCurrentCoord
        extendedInfoCount = 0
    }

    /**
     * Writes the extended info blocks over to the backing buffer, based on the indices
     * of the NPCs from whom we requested extended info updates prior in this cycle.
     */
    internal fun putExtendedInfo() {
        val jagBuffer = backingBuffer().toJagByteBuf()
        for (i in 0 until extendedInfoCount) {
            val index = extendedInfoIndices[i].toInt()
            val other = checkNotNull(repository.getOrNull(index))
            val observerFlag = other.extendedInfo.flags or observerExtendedInfoFlags.getFlag(i)
            other.extendedInfo.pExtendedInfo(
                platformType,
                jagBuffer,
                observerFlag,
                extendedInfoCount - i,
            )
        }
    }

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
    private fun defragmentIndices() {
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
     * Processes high resolution, existing, NPCs by writing their movements/extended info updates,
     * or removes them altogether if need be.
     * @param buffer the buffer into which to write the bitcode information.
     * @param viewDistance the maximum view distance how far a NPC can be seen.
     * If the npc is farther away from the local player than the provided view distance,
     * they will be removed from high resolution view.
     * @return whether any high resolution npcs were removed in the middle of the
     * array. This does not include the npcs dropped off at the end.
     * This is necessary to determine whether we need to defragment the array (ie remove any
     * gaps that were produced by removing npcs in the middle of the array).
     */
    private fun processHighResolution(
        buffer: BitBuf,
        viewDistance: Int,
    ): Boolean {
        // If no one to process, skip
        if (this.highResolutionNpcIndexCount == 0) {
            buffer.pBits(8, 0)
            return false
        }
        // If our coordinate compared to last cycle changed more than 'viewDistance'
        // tiles, every NPC in our local view would be removed anyhow,
        // so by sending the count as 0, client automatically removes everyone
        if (isTooFar(viewDistance)) {
            buffer.pBits(8, 0)
            // While it would be more efficient to just... not do this block below,
            // the reality is there are ~25k static npcs in the game alone,
            // and by tracking the observer counts we can omit computing
            // extended info as well as high resolution movement blocks
            // for any npc that doesn't have a player near them,
            // which, even at full world, will be the majority of npcs.
            for (i in 0..<highResolutionNpcIndexCount) {
                val npcIndex = highResolutionNpcIndices[i].toInt()
                val avatar = repository.getOrNull(npcIndex) ?: continue
                avatar.removeObserver()
            }
            highResolutionNpcIndexCount = 0
            return false
        }
        // Iterate NPCs in a backwards order until the first npc who should not be removed
        // everyone else will be automatically dropped off by the client if the count
        // transmitted is less than what the client currently knows about
        for (i in highResolutionNpcIndexCount - 1 downTo 0) {
            val npcIndex = highResolutionNpcIndices[i].toInt()
            val avatar = repository.getOrNull(npcIndex)
            if (!removeHighResolutionNpc(avatar, viewDistance)) {
                break
            }
            avatar?.removeObserver()
            highResolutionNpcIndexCount--
        }
        val processedCount = this.highResolutionNpcIndexCount
        buffer.pBits(8, processedCount)
        for (i in 0..<processedCount) {
            val npcIndex = highResolutionNpcIndices[i].toInt()
            val avatar = repository.getOrNull(npcIndex)
            if (removeHighResolutionNpc(avatar, viewDistance)) {
                buffer.pBits(1, 1)
                buffer.pBits(2, 3)
                avatar?.removeObserver()
                highResolutionNpcIndexCount--
                continue
            }
            if (avatar.extendedInfo.flags != 0) {
                extendedInfoIndices[extendedInfoCount++] = npcIndex.toUShort()
            }
            val movementBuffer = checkNotNull(avatar.highResMovementBuffer)
            buffer.pBits(movementBuffer)
        }
        return processedCount != highResolutionNpcIndexCount
    }

    /**
     * Checks whether to remove a high resolution npc from the high resolution view.
     * @param avatar the avatar of the high resolution npc, or null.
     * @param viewDistance the maximum view distance how far the npc can be without
     * being removed.
     * @return whether to remove the npc from the high resolution view.
     */
    @OptIn(ExperimentalContracts::class)
    private fun removeHighResolutionNpc(
        avatar: NpcAvatar?,
        viewDistance: Int,
    ): Boolean {
        contract {
            returns(false) implies (avatar != null)
        }
        return avatar == null ||
            avatar.details.inaccessible ||
            avatar.details.isTeleporting() ||
            !withinDistance(localPlayerCurrentCoord, avatar.details.currentCoord, viewDistance)
    }

    /**
     * Checks if the player has moved a greater distance from their previous coordinate
     * than the maximum [viewDistance], in which case all existing high resolution NPCs
     * can be removed in one go in a more efficient manner.
     * @param viewDistance the maximum view distance how far a player can see other npcs
     * @return whether the player has moved a greater distance than [viewDistance] since
     * the last cycle.
     */
    private fun isTooFar(viewDistance: Int): Boolean {
        return !withinDistance(
            this.localPlayerLastCoord,
            this.localPlayerCurrentCoord,
            viewDistance,
        )
    }

    /**
     * Processes the NPCs that are in low resolution by requesting an iterator of NPC indices
     * within [viewDistance] of the local player's current coordinate.
     * This function is responsible for deciding which NPCs to move to high resolution,
     * and ignore which ones are already in high resolution. It is the server's duty to always
     * return all the NPCs that should be added, regardless of if they were previously already
     * added.
     * @param buffer the primary buffer into which to write the bitcode information
     * @param viewDistance the maximum view distance how far a npc can be from the local
     * player to still be considered in high resolution.
     */
    private fun processLowResolution(
        buffer: BitBuf,
        viewDistance: Int,
    ) {
        // If our local view is already maxed out, don't even request for indices
        if (this.highResolutionNpcIndexCount >= MAX_HIGH_RESOLUTION_NPCS) {
            return
        }
        val encoder = lowResolutionToHighResolutionEncoders[platformType]
        val largeDistance = viewDistance > MAX_SMALL_PACKET_DISTANCE
        val npcs =
            this.indexSupplier.supply(
                localPlayerIndex,
                localPlayerCurrentCoord.level,
                localPlayerCurrentCoord.x,
                localPlayerCurrentCoord.z,
                viewDistance,
            )
        while (npcs.hasNext()) {
            val index = npcs.next() and NPC_INFO_CAPACITY
            if (index == NPC_INFO_CAPACITY || isHighResolution(index)) {
                continue
            }
            if (this.highResolutionNpcIndexCount >= MAX_HIGH_RESOLUTION_NPCS) {
                break
            }
            val avatar = repository.getOrNull(index) ?: continue
            avatar.addObserver()
            val i = highResolutionNpcIndexCount++
            highResolutionNpcIndices[i] = index.toUShort()
            val observerFlags = avatar.extendedInfo.getLowToHighResChangeExtendedInfoFlags()
            this.observerExtendedInfoFlags.addFlag(i, observerFlags)
            val extendedInfo = (avatar.extendedInfo.flags or observerFlags) != 0
            encoder.encode(
                buffer,
                avatar.details,
                extendedInfo,
                localPlayerCurrentCoord,
                largeDistance,
            )
        }
    }

    /**
     * Checks whether a npc by the index of [index] is already within our high resolution
     * view.
     * @param index the index of the npc to check
     * @return whether the npc at the given index is already in high resolution.
     */
    private fun isHighResolution(index: Int): Boolean {
        // NOTE: Perhaps it's more efficient to just allocate 65535 bits and do a bit check?
        // Would cost ~16.76mb at max world capacity
        for (i in 0..<highResolutionNpcIndexCount) {
            if (highResolutionNpcIndices[i].toInt() == index) {
                return true
            }
        }
        return false
    }

    /**
     * Checks whether the [coord] is within [distance] of the [localPlayerCoordGrid].
     * @return whether the coord is within distance of the local player's current coordinate.
     */
    private fun withinDistance(
        localPlayerCoordGrid: CoordGrid,
        coord: CoordGrid,
        distance: Int,
    ): Boolean {
        return localPlayerCoordGrid.inDistance(
            coord,
            distance,
        )
    }

    override fun onAlloc(
        index: Int,
        platformType: PlatformType,
    ) {
        this.localPlayerIndex = index
        this.platformType = platformType
        this.localPlayerLastCoord = CoordGrid.INVALID
        this.viewDistance = MAX_SMALL_PACKET_DISTANCE
        this.highResolutionNpcIndexCount = 0
        this.extendedInfoCount = 0
        this.observerExtendedInfoFlags.reset()
    }

    override fun onDealloc() {
        this.buffer = null
    }

    private companion object {
        /**
         * The default capacity of the backing byte buffer into which all player info is written.
         */
        private const val BUF_CAPACITY: Int = 40_000

        /**
         * The maximum view distance that can be transmitted using the smaller npc info packet.
         */
        private const val MAX_SMALL_PACKET_DISTANCE: Int = 15

        /**
         * The maximum number of high resolution NPCs that the client supports, limited by the
         * client's array of extended info updates being a size-250 int array.
         */
        private const val MAX_HIGH_RESOLUTION_NPCS: Int = 250

        /**
         * The terminator value used to indicate that no NPC is here.
         */
        private const val NPC_INDEX_TERMINATOR: UShort = 0xFFFFu

        /**
         * Maximum unsigned short constant, the capacity of the npc info protocol.
         */
        private const val NPC_INFO_CAPACITY = 0xFFFF
    }
}
