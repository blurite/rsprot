@file:Suppress("DuplicatedCode")

package net.rsprot.protocol.game.outgoing.info.npcinfo

import com.github.michaelbull.logging.InlineLogger
import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufAllocator
import net.rsprot.buffer.bitbuffer.BitBuf
import net.rsprot.buffer.bitbuffer.toBitBuf
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprot.protocol.common.client.OldSchoolClientType
import net.rsprot.protocol.game.outgoing.info.ByteBufRecycler
import net.rsprot.protocol.game.outgoing.info.exceptions.InfoProcessException
import net.rsprot.protocol.game.outgoing.info.util.PacketResult
import net.rsprot.protocol.game.outgoing.info.util.ReferencePooledObject
import net.rsprot.protocol.game.outgoing.info.worldentityinfo.WorldEntityInfo
import net.rsprot.protocol.internal.checkCommunicationThread
import net.rsprot.protocol.internal.client.ClientTypeMap
import net.rsprot.protocol.internal.game.outgoing.info.CoordGrid
import net.rsprot.protocol.internal.game.outgoing.info.npcinfo.encoder.NpcResolutionChangeEncoder
import net.rsprot.protocol.internal.game.outgoing.info.util.ZoneIndexStorage
import net.rsprot.protocol.message.ConsumableMessage
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.math.max

/**
 * An implementation of the npc info packet.
 * This class is responsible for bringing together all the bits of the npc info protocol,
 * including copying all the pre-built buffers that were made beforehand.
 * @property allocator the byte buffer allocator used to allocate new buffers to be used
 * for the npc info packet, as well as the pre-built extended info buffers.
 * @property repository the npc avatar repository, keeping track of every npc avatar that exists
 * in the game.
 * @property oldSchoolClientType the client the player owning this npc info packet is on
 * @property localPlayerIndex the index of the local player that owns this npc info packet.
 * @property zoneIndexStorage the zone index storage is used to look up the indices of NPCs near
 * the player in an efficient manner.
 * @property lowResolutionToHighResolutionEncoders a client map of low resolution to high resolution
 * change encoders, used to move a npc into high resolution for the given player.
 * As this is scrambled, a separate client-specific implementation is required.
 * @property filter a npc avatar filter that must be passed to add/keep a npc in high resolution.
 */
@OptIn(ExperimentalUnsignedTypes::class)
@Suppress("ReplaceUntilWithRangeUntil")
public class NpcInfo internal constructor(
    private val allocator: ByteBufAllocator,
    private val repository: NpcAvatarRepository,
    private var oldSchoolClientType: OldSchoolClientType,
    internal var localPlayerIndex: Int,
    private val zoneIndexStorage: ZoneIndexStorage,
    private val lowResolutionToHighResolutionEncoders: ClientTypeMap<NpcResolutionChangeEncoder>,
    private val detailsStorage: NpcInfoWorldDetailsStorage,
    private val recycler: ByteBufRecycler,
    private val filter: NpcAvatarFilter?,
    private var worldEntityInfo: WorldEntityInfo?,
) : ReferencePooledObject {
    /**
     * The maximum render distance how far a player will see other NPCs.
     * Unlike with player info, this does not automatically resize to accommodate for nearby NPCs,
     * as it is almost impossible for such a scenario to happen in the first place.
     * It is confirmed that OldSchool RuneScape does not do it either.
     */
    private var renderDistance: Int = DEFAULT_DISTANCE

    /**
     * The radius in zones (8x8 tile blocks) to iterate over, in order to find NPCs to add
     * to high resolution. After this iteration, the npc must still pass the distance checks,
     * as well as build area checks.
     */
    private var zoneSearchRadius: Int = DEFAULT_ZONE_SEARCH_RADIUS

    /**
     * The exception that was caught during the processing of this player's npc info packet.
     * This exception will be propagated further during the [toPacketResult] function call,
     * allowing the server to handle it properly at a per-player basis.
     */
    @Volatile
    internal var exception: Exception? = null

    /**
     * An array of world details, containing all the player info properties specific to a single world.
     * The root world is placed at the end of this array, however id -1 will be treated as the root.
     */
    internal val details: Array<NpcInfoWorldDetails?> = arrayOfNulls(WORLD_ENTITY_CAPACITY + 1)

    /**
     * The last cycle's coordinate of the local player, used to perform faster npc removal.
     * If the player moves a greater distance than the [NpcInfo.renderDistance], we can make the assumption
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
     * An array of NPCs which are marked as specific-visible. Any NPC avatar that was explicitly marked
     * as visible-to-specific-only will only render to players that mark that avatar's index as specific
     * visible. Anyone else will be unable to see such NPCs.
     */
    internal val specificVisible: LongArray = LongArray((NPC_INFO_CAPACITY + 1) ushr 6)

    override fun isDestroyed(): Boolean = this.exception != null

    /**
     * Allocates a new NPC info tracking object for the respective [worldId],
     * keeping track of everyone that's within this new world entity.
     * @param worldId the new world entity id
     */
    public fun allocateWorld(worldId: Int) {
        checkCommunicationThread()
        if (isDestroyed()) return
        require(worldId in 0..<WORLD_ENTITY_CAPACITY) {
            "World id out of bounds: $worldId"
        }
        val existing = details[worldId]
        require(existing == null) {
            "World $worldId already allocated."
        }
        details[worldId] = detailsStorage.poll(worldId)
    }

    /**
     * Destroys NPC info tracking for the specified [worldId].
     * This is intended to be used when one of the world entities leaves the render distance.
     */
    public fun destroyWorld(worldId: Int) {
        checkCommunicationThread()
        if (isDestroyed()) return
        require(worldId in 0..<WORLD_ENTITY_CAPACITY) {
            "World id out of bounds: $worldId"
        }
        val existing = details[worldId]
        require(existing != null) {
            "World $worldId does not exist."
        }
        releaseObservers(existing)
        detailsStorage.push(existing)
        details[worldId] = null
    }

    /**
     * Gets the world details implementation of the specified [worldId].
     */
    private fun getDetails(worldId: Int): NpcInfoWorldDetails {
        val details =
            if (worldId == ROOT_WORLD) {
                details[WORLD_ENTITY_CAPACITY]
            } else {
                require(worldId in 0..<WORLD_ENTITY_CAPACITY) {
                    "World id out of bounds: $worldId"
                }
                details[worldId]
            }
        return checkNotNull(details) {
            "World info details not allocated for world $worldId"
        }
    }

    /**
     * Gets the world details implementation of the specified [worldId], or null if it doesn't exist.
     * This function will throw an exception if it is called post-deallocation for the root world,
     * as it will then be deallocated.
     */
    private fun getDetailsOrNull(worldId: Int): NpcInfoWorldDetails? {
        val details =
            if (worldId == ROOT_WORLD) {
                details[WORLD_ENTITY_CAPACITY]
            } else {
                if (worldId !in 0..<WORLD_ENTITY_CAPACITY) {
                    return null
                }
                details[worldId]
            }
        return details
    }

    /**
     * Gets the high resolution indices of the given [worldId] in a new arraylist of integers.
     * The list is initialized to an initial capacity equal to the high resolution npc index count.
     * @param worldId the worldId to collect the indices from. For root world, use [ROOT_WORLD].
     * @throws IllegalArgumentException if the world id is not in range of 0..<2048, or [ROOT_WORLD].
     * @throws IllegalStateException if the provided world has not been allocated. It is up to the
     * caller to ensure the world they're accessible is available. Root world will always be available
     * as long as the given info object is allocated.
     * @return the newly created arraylist of indices
     */
    public fun getHighResolutionIndices(worldId: Int): ArrayList<Int> {
        checkCommunicationThread()
        if (isDestroyed()) return ArrayList(0)
        val details = getDetails(worldId)
        val collection = ArrayList<Int>(details.highResolutionNpcIndexCount)
        for (i in 0..<details.highResolutionNpcIndexCount) {
            val index = details.highResolutionNpcIndices[i].toInt()
            collection.add(index)
        }
        return collection
    }

    /**
     * Gets the high resolution indices of the given [worldId] in a new arraylist of integers, or null
     * if the provided world does not exist.
     * The list is initialized to an initial capacity equal to the high resolution npc index count.
     * @param worldId the worldId to collect the indices from. For root world, use [ROOT_WORLD].
     * @throws IllegalArgumentException if the world id is not in range of 0..<2048, or [ROOT_WORLD].
     * @throws IllegalStateException if the provided world has not been allocated. It is up to the
     * caller to ensure the world they're accessible is available. Root world will always be available
     * as long as the given info object is allocated.
     * @return the newly created arraylist of indices, or null if the world does not exist.
     */
    public fun getHighResolutionIndicesOrNull(worldId: Int): ArrayList<Int>? {
        checkCommunicationThread()
        if (isDestroyed()) return null
        val details = getDetailsOrNull(worldId) ?: return null
        val collection = ArrayList<Int>(details.highResolutionNpcIndexCount)
        for (i in 0..<details.highResolutionNpcIndexCount) {
            val index = details.highResolutionNpcIndices[i].toInt()
            collection.add(index)
        }
        return collection
    }

    /**
     * Appends the high resolution indices of the given [worldId] to the provided
     * [collection]. This can be used to determine which NPCs the player is currently
     * seeing in the client. Servers often rely on this metric to determine things
     * such as aggression/hunt.
     * @param worldId the worldId to collect the indices from. For root world, use [ROOT_WORLD].
     * @param collection the mutable collection of integer indices to append the indices into.
     * @param throwExceptionIfNoWorld whether to throw an exception if the world does not exist.
     * @throws IllegalArgumentException if the world id is not in range of 0..<2048, or [ROOT_WORLD],
     * as long as [throwExceptionIfNoWorld] is true.
     * @throws IllegalStateException if the provided world has not been allocated. It is up to the
     * caller to ensure the world they're accessible is available. Root world will always be available
     * as long as the given info object is allocated. This will only be thrown if [throwExceptionIfNoWorld]
     * is true.
     * @return the provided [collection] to chaining.
     */
    @JvmOverloads
    public fun <T> appendHighResolutionIndices(
        worldId: Int,
        collection: T,
        throwExceptionIfNoWorld: Boolean = true,
    ): T where T : MutableCollection<Int> {
        checkCommunicationThread()
        if (isDestroyed()) return collection
        val details =
            if (throwExceptionIfNoWorld) {
                getDetails(worldId)
            } else {
                getDetailsOrNull(worldId) ?: return collection
            }
        for (i in 0..<details.highResolutionNpcIndexCount) {
            val index = details.highResolutionNpcIndices[i].toInt()
            collection.add(index)
        }
        return collection
    }

    /**
     * Returns the backing byte buffer holding all the computed information.
     * @throws IllegalStateException if the buffer is null, meaning it has no yet been
     * initialized for this cycle.
     */
    @Throws(IllegalStateException::class)
    private fun backingBuffer(worldId: Int): ByteBuf = checkNotNull(getDetails(worldId).buffer)

    /**
     * Returns the backing byte buffer holding all the computed information.
     * @throws IllegalStateException if the buffer is null, meaning it has no yet been
     * initialized for this cycle.
     */
    @Throws(IllegalStateException::class)
    private fun backingBuffer(details: NpcInfoWorldDetails): ByteBuf = checkNotNull(details.buffer)

    /**
     * Sets the render distance, meaning how far the player can see NPCs.
     * @param num the distance from which NPCs become visible
     */
    @Deprecated(
        "Deprecated for consistent naming. Use setRenderDistance()",
        ReplaceWith("setRenderDistance(num)"),
    )
    public fun setViewDistance(num: Int) {
        setRenderDistance(num)
    }

    /**
     * Sets the render distance, meaning how far the player can see NPCs.
     * Note that you may need to also call [setZoneSearchRadius] to increase
     * the search range for new NPCs.
     * @param num the distance from which NPCs become visible
     */
    public fun setRenderDistance(num: Int) {
        checkCommunicationThread()
        if (isDestroyed()) return
        this.renderDistance = num
    }

    /**
     * Resets the render distance back to a default value of 15 tile radius.
     */
    @Deprecated(
        "Deprecated for consistent naming. Use resetRenderDistance()",
        ReplaceWith("resetRenderDistance()"),
    )
    public fun resetViewDistance() {
        resetRenderDistance()
    }

    /**
     * Resets the render distance back to a default value of 15 tile radius.
     */
    public fun resetRenderDistance() {
        checkCommunicationThread()
        if (isDestroyed()) return
        this.renderDistance = DEFAULT_DISTANCE
    }

    /**
     * Sets the search radius in zones (8x8 tile blocks) to look for NPCs around the player's
     * current coordinate.
     * @param radius the radius in zones to search for. The default is 3.
     * Note that setting the radius negative disables searching for NPCs altogether,
     * and ends up removing any existing ones.
     */
    public fun setZoneSearchRadius(radius: Int) {
        checkCommunicationThread()
        if (isDestroyed()) return
        this.zoneSearchRadius = radius
    }

    /**
     * Sets the priority threshold caps for how many NPCs can render at once in
     * either of the priority groups.
     *
     * It is important to note that if the priority caps are modified at "runtime" (as in, once NPCs
     * are already being tracked), any existing NPCs which are being tracked will not be cleared out
     * by calling this function. It will only prevent new additions from taking place beyond the new
     * limits, but one would have to wait until the counts naturally decrement down in order to hit
     * the desired limits.
     *
     * The intended use-case here is to deprioritize dynamic NPCs such as pets which could fill up
     * the entire high resolution with just pets, preventing more important NPCs, such as shopkeepers
     * from rendering to the player. By restricting low priority to say 50 NPCs, and normal priority
     * to 99, as long as we correctly flag the pet NPCs as low priority, we ensure that no more than
     * 50 pets can ever render at once, leaving those 99 remaining slots for any NPCs that are deemed
     * more important.
     *
     * Due to the structure of the NPC info protocol, it is not viable to do an implementation where
     * the high resolution is consistently capped out (e.g. allow up to 149 pets, but if more important
     * NPCs come into range, drop some pets and render the higher resolution NPCs instead). This would
     * be computationally heavy to check as the protocol first goes over any existing high resolution
     * NPCs, which means we lack any context over how many higher resolution NPCs in need of rendering.
     * Even if we allow the one tick delay to occur here, the implementation would be quite tricky and
     * is not worth the headache it causes.
     *
     * @param worldId the world id to set the caps for.
     * @param lowPriorityCap the maximum number of NPCs that can render at once with the low priority.
     * If the low priority cap has been reached, no more NPCs with the low priority will be able to be
     * added to high resolution.
     * @param normalPrioritySoftCap the maximum number of normal priority NPCs that can render at once.
     * Note that if the normal priority cap is reached, the low priority group will be utilized instead.
     * In such cases, it is possible to end up with more normal priority NPCs than what is indicated by
     * the soft cap.
     */
    public fun setPriorityCaps(
        worldId: Int,
        lowPriorityCap: Int,
        normalPrioritySoftCap: Int,
    ) {
        if (isDestroyed()) return
        require(lowPriorityCap >= 0) {
            "Low priority cap cannot be negative."
        }
        require(normalPrioritySoftCap >= 0) {
            "Normal priority soft cap cannot be negative."
        }
        require(lowPriorityCap + normalPrioritySoftCap <= MAX_HIGH_RESOLUTION_NPCS) {
            "The sum of low priority cap and normal priority soft cap must be $MAX_HIGH_RESOLUTION_NPCS or fewer."
        }
        val world = getDetails(worldId)
        world.lowPriorityCap = lowPriorityCap
        world.normalPrioritySoftCap = normalPrioritySoftCap
    }

    /**
     * Marks the specified NPC's [avatar] as specific-visible, meaning the NPC will render
     * to this player if other conditions are met. Anyone that hasn't marked it as specific
     * will be unable to see that NPC.
     * @param avatar the NPC avatar whom to mark as specific-visible.
     * @throws IllegalArgumentException if the [avatar] was not allocated as specific-only.
     */
    public fun setSpecific(avatar: NpcAvatar) {
        if (isDestroyed()) return
        require(avatar.details.specific) {
            "Only avatars that are marked as specific-only can be marked as specific."
        }
        setSpecific(avatar.details.index)
    }

    /**
     * Clears the specified NPC's [avatar] as specific-visible.
     * @param avatar the NPC avatar whom to unmark as specific-visible.
     * @throws IllegalArgumentException if the [avatar] was not allocated as specific-only.
     */
    public fun clearSpecific(avatar: NpcAvatar) {
        if (isDestroyed()) return
        require(avatar.details.specific) {
            "Only avatars that are marked as specific-only can be unmarked as specific."
        }
        unsetSpecific(avatar.details.index)
    }

    /**
     * Checks whether the [avatar] is specific-visible.
     * @param avatar the avatar of the NPC whom to check.
     * @return whether the NPC has been marked as specific-visible.
     */
    public fun isSpecific(avatar: NpcAvatar): Boolean {
        if (isDestroyed()) return false
        return isSpecific(avatar.details.index)
    }

    /**
     * Gets a new instance of an ArrayList containing the indices of all the NPCs that are
     * still marked as specific to us. Note that any NPC which was originally marked as
     * specific, but got deallocated at some point will not be part of this collection,
     * as deallocated NPCs automatically unset as specific on all relevant players.
     *
     * This function is best used before a player logs out, to clear any associated specific
     * NPCs. The returned collection is a new mutable ArrayList - servers are free to
     * utilize or mutate this however they want, should they wish to do so. Note that
     * this function needs to be called before deallocating NPC info.
     *
     * @return an ArrayList of NPC indices that are marked as specific and have not yet
     * been deallocated from the game. These NPCs may still be in the inaccessible AKA dead state.
     */
    public fun getSpecificIndices(): ArrayList<Int> {
        if (isDestroyed()) return ArrayList(0)
        val list = ArrayList<Int>(0)
        val array = this.specificVisible
        for (i in array.indices) {
            val vis = array[i]
            // Quickly skip over 64 NPCs if there are no specifics
            if (vis == 0L) continue
            // Otherwise, do a regular length-64 iteration
            // While this could be improved with more complicated nextSetBit() computations,
            // given the nature of this function and how rarely specific NPCs are actually used,
            // it is not worth the hassle.
            val start = i * Long.SIZE_BITS
            val end = start + Long.SIZE_BITS
            for (index in start..<end) {
                if (isSpecific(index)) {
                    list.add(index)
                }
            }
        }
        return list
    }

    /**
     * Checks whether the NPC at the specified [index] is specific-visible.
     * @param index the absolute index of the NPC to check.
     * @return whether the NPC has been marked as specific-visible.
     */
    private fun isSpecific(index: Int): Boolean {
        val longIndex = index ushr 6
        val bit = 1L shl (index and 0x3F)
        return this.specificVisible[longIndex] and bit != 0L
    }

    /**
     * Sets the NPC at index [index] as specific-visible.
     * @param index the absolute index of the NPC to set as specific.
     */
    private fun setSpecific(index: Int) {
        val longIndex = index ushr 6
        val bit = 1L shl (index and 0x3F)
        val cur = this.specificVisible[longIndex]
        this.specificVisible[longIndex] = cur or bit
    }

    /**
     * Clears the specific [index] flag from the specific visible NPCs bit array.
     * @param index the absolute index of the NPC to clear.
     */
    internal fun unsetSpecific(index: Int) {
        val longIndex = index ushr 6
        val bit = 1L shl (index and 0x3F)
        val cur = this.specificVisible[longIndex]
        this.specificVisible[longIndex] = cur and bit.inv()
    }

    /**
     * Synchronizes the worlds based on world entity info, by removing/allocating
     * world info details depending on which worlds are available.
     */
    internal fun synchronizeWorlds() {
        val info =
            worldEntityInfo
                ?: error("World entity info not available")
        for (index in info.getRemovedWorldEntityIndices()) {
            this.destroyWorld(index)
        }
        for (index in info.getAddedWorldEntityIndices()) {
            this.allocateWorld(index)
        }
    }

    @Suppress("NOTHING_TO_INLINE")
    @JvmSynthetic
    public inline fun internalPacketResult(worldId: Int): PacketResult<NpcInfoPacket> {
        return toPacketResult(worldId)
    }

    /**
     * Turns the previously-computed npc info into a packet instance
     * which can be flushed to the client, or an exception if one was thrown while
     * building the packet.
     * @return the npc packet instance in a [PacketResult].
     */
    @PublishedApi
    internal fun toPacketResult(worldId: Int): PacketResult<NpcInfoPacket> {
        val exception = this.exception
        if (exception != null) {
            return PacketResult.failure(
                InfoProcessException(
                    "Exception occurred during npc info processing for index $localPlayerIndex",
                    exception,
                ),
            )
        }
        val details =
            getDetailsOrNull(worldId)
                ?: return PacketResult.failure(
                    IllegalStateException("World $worldId does not exist."),
                )
        val previousPacket =
            details.previousPacket
                ?: return PacketResult.failure(
                    IllegalStateException("Previous npc info packet not calculated."),
                )

        return PacketResult.success(previousPacket)
    }

    /**
     * Allocates a new buffer from the [allocator] with a capacity of [BUF_CAPACITY].
     * The old [NpcInfoWorldDetails.buffer] will not be released, as that is the duty of the encoder class.
     */
    @Suppress("DuplicatedCode")
    private fun allocBuffer(worldId: Int): ByteBuf {
        val details = getDetails(worldId)
        // Acquire a new buffer with each cycle, in case the previous one isn't fully written out yet
        val buffer = allocator.buffer(BUF_CAPACITY, BUF_CAPACITY)
        details.buffer = buffer
        details.lastCycleHighResolutionNpcIndexCount = details.highResolutionNpcIndexCount
        recycler += buffer
        return buffer
    }

    /**
     * Updates the root world coordinate of the local player.
     * @param coordGrid the coordgrid of the player
     */
    internal fun updateRootCoord(coordGrid: CoordGrid) {
        checkCommunicationThread()
        if (isDestroyed()) return
        this.localPlayerCurrentCoord = coordGrid
    }

    /**
     * Computes the high resolution and low resolution bitcodes for this given player,
     * additionally marks down which NPCs need to furthermore send their extended info
     * updates.
     */
    internal fun compute(details: NpcInfoWorldDetails) {
        val renderDistance = this.renderDistance
        val zoneSearchRadius = this.zoneSearchRadius
        val buffer = allocBuffer(details.worldId)
        buffer.toBitBuf().use { bitBuffer ->
            val fragmented = processHighResolution(details, bitBuffer, renderDistance, zoneSearchRadius)
            if (fragmented) {
                details.defragmentIndices()
            }
            if (details.worldId == ROOT_WORLD) {
                processRootWorldLowResolution(details, bitBuffer, renderDistance, zoneSearchRadius)
            } else {
                processLowResolution(details, bitBuffer)
            }
            // Terminate the low-resolution processing block if there are extended info
            // blocks after that; if not, the loop ends naturally due to not enough
            // readable bits remaining (at most would have 7 bits remaining due to
            // the bit writer closing, which "finishes" the current byte).
            if (details.extendedInfoCount > 0) {
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
    internal fun afterUpdate() {
        this.localPlayerLastCoord = this.localPlayerCurrentCoord
        for (details in this.details) {
            if (details == null) {
                continue
            }
            details.extendedInfoCount = 0
            details.observerExtendedInfoFlags.reset()

            val previousPacket = details.previousPacket
            if (previousPacket is ConsumableMessage) {
                if (!previousPacket.isConsumed()) {
                    logger.warn {
                        "Previous npc info packet was calculated but " +
                            "not sent out to the client for world ${details.worldId} for player $localPlayerIndex!"
                    }
                }
            }
            val buffer = backingBuffer(details.worldId)
            val isEmpty = isEmptyPacket(details, buffer)
            details.previousPacket =
                if (details.largeUpdate) {
                    NpcInfoLargeV5(buffer, isEmpty)
                } else {
                    NpcInfoSmallV5(buffer, isEmpty)
                }

            details.largeUpdate = false
        }
    }

    /**
     * Checks if the NPC info packet can be considered as fully empty.
     * This means there were no high resolution NPCs in the last cycle,
     * nor are there any in this cycle.
     * @param details
     */
    private fun isEmptyPacket(
        details: NpcInfoWorldDetails,
        buffer: ByteBuf,
    ): Boolean {
        // If there were any high resolution NPCs in the last cycle, it cannot be considered empty,
        // as it is possible for us to just send the new count as 0 that tells the client to
        // clear all high resolution NPCs.
        if (details.lastCycleHighResolutionNpcIndexCount != 0) {
            return false
        }
        val readableBytes = buffer.readableBytes()
        if (readableBytes == 0) {
            return true
        }
        if (readableBytes > 1) {
            return false
        }
        // Only return true if the new high resolution NPC count is also zero.
        return buffer.getByte(buffer.readerIndex()).toInt() == 0
    }

    /**
     * Writes the extended info blocks over to the backing buffer, based on the indices
     * of the NPCs from whom we requested extended info updates prior in this cycle.
     */
    internal fun putExtendedInfo(details: NpcInfoWorldDetails) {
        val jagBuffer = backingBuffer(details).toJagByteBuf()
        for (i in 0 until details.extendedInfoCount) {
            val index = details.extendedInfoIndices[i].toInt()
            val other = repository.getOrNull(index)
            if (other == null) {
                // If other is null at this point, it means it was destroyed mid-processing at an earlier
                // stage. In order to avoid the issue escalating further by throwing errors for every player
                // that was in vicinity of the NPC that got destroyed, we simply write no-mask-update,
                // even though a mask update was requested at an earlier stage.
                // The next game tick, the NPC will be removed as the info is null, which is one of
                // the conditions for removing a NPC from tracking.
                jagBuffer.p1(0)
                continue
            }
            val observerFlag = details.observerExtendedInfoFlags.getFlag(i)
            other.extendedInfo.pExtendedInfo(
                oldSchoolClientType,
                jagBuffer,
                localPlayerIndex,
                i,
                details.extendedInfoCount - i,
                observerFlag,
            )
        }
    }

    /**
     * Processes high resolution, existing, NPCs by writing their movements/extended info updates,
     * or removes them altogether if need be.
     * @param buffer the buffer into which to write the bitcode information.
     * @param renderDistance the maximum render distance how far a NPC can be seen.
     * If the npc is farther away from the local player than the provided render distance,
     * they will be removed from high resolution view.
     * @param zoneSearchRadius the number of zones to search for.
     * @return whether any high resolution npcs were removed in the middle of the
     * array. This does not include the npcs dropped off at the end.
     * This is necessary to determine whether we need to defragment the array (ie remove any
     * gaps that were produced by removing npcs in the middle of the array).
     */
    private fun processHighResolution(
        details: NpcInfoWorldDetails,
        buffer: BitBuf,
        renderDistance: Int,
        zoneSearchRadius: Int,
    ): Boolean {
        // If no one to process, skip
        if (details.highResolutionNpcIndexCount == 0) {
            buffer.pBits(8, 0)
            return false
        }
        val worldEntityInfo =
            checkNotNull(this.worldEntityInfo) {
                "World entity info is null"
            }
        // If our coordinate compared to last cycle changed more than 'radius'
        // tiles, every NPC in our local view would be removed anyhow,
        // so by sending the count as 0, client automatically removes everyone
        if (zoneSearchRadius < 0 ||
            !worldEntityInfo.isVisible(
                localPlayerLastCoord,
                localPlayerCurrentCoord,
                max((zoneSearchRadius shl 3) + 7, renderDistance),
            )
        ) {
            buffer.pBits(8, 0)
            // While it would be more efficient to just... not do this block below,
            // the reality is there are ~25k static npcs in the game alone,
            // and by tracking the observer counts we can omit computing
            // extended info as well as high resolution movement blocks
            // for any npc that doesn't have a player near them,
            // which, even at full world, will be the majority of npcs.
            for (i in 0..<details.highResolutionNpcIndexCount) {
                val npcIndex = details.highResolutionNpcIndices[i].toInt()
                val avatar = repository.getOrNull(npcIndex) ?: continue
                avatar.removeObserver(localPlayerIndex)
            }
            details.highResolutionNpcIndexCount = 0
            details.clearPriorities()
            return false
        }
        // Iterate NPCs in a backwards order until the first npc who should not be removed
        // everyone else will be automatically dropped off by the client if the count
        // transmitted is less than what the client currently knows about
        for (i in details.highResolutionNpcIndexCount - 1 downTo 0) {
            val npcIndex = details.highResolutionNpcIndices[i].toInt()
            val avatar = repository.getOrNull(npcIndex)
            if (!removeHighResolutionNpc(
                    worldEntityInfo,
                    avatar,
                    renderDistance,
                )
            ) {
                break
            }
            details.highResolutionNpcIndexCount--
            avatar?.removeObserver(localPlayerIndex)
            details.decrementPriority(i)
        }
        val processedCount = details.highResolutionNpcIndexCount
        buffer.pBits(8, processedCount)
        for (i in 0..<processedCount) {
            val npcIndex = details.highResolutionNpcIndices[i].toInt()
            val avatar = repository.getOrNull(npcIndex)
            if (removeHighResolutionNpc(
                    worldEntityInfo,
                    avatar,
                    renderDistance,
                )
            ) {
                buffer.pBits(1, 1)
                buffer.pBits(2, 3)
                details.highResolutionNpcIndices[i] = NPC_INDEX_TERMINATOR
                details.highResolutionNpcIndexCount--
                avatar?.removeObserver(localPlayerIndex)
                details.decrementPriority(i)
                continue
            }
            if (avatar.extendedInfo.flags != 0) {
                details.extendedInfoIndices[details.extendedInfoCount++] = npcIndex.toUShort()
            }
            val movementBuffer =
                checkNotNull(avatar.highResMovementBuffer) {
                    "High resolution movement buffer is null for $avatar"
                }
            buffer.pBits(movementBuffer)
        }
        return processedCount != details.highResolutionNpcIndexCount
    }

    /**
     * Checks whether to remove a high resolution npc from the high resolution view.
     * @param avatar the avatar of the high resolution npc, or null.
     * @param renderDistance the maximum render distance how far the npc can be without
     * being removed.
     * @return whether to remove the npc from the high resolution view.
     */
    @OptIn(ExperimentalContracts::class)
    private fun removeHighResolutionNpc(
        worldEntityInfo: WorldEntityInfo,
        avatar: NpcAvatar?,
        renderDistance: Int,
    ): Boolean {
        contract {
            returns(false) implies (avatar != null)
        }
        if (avatar == null ||
            avatar.details.inaccessible ||
            avatar.details.isTeleporting() ||
            avatar.details.allocateCycle == NpcInfoProtocol.cycleCount
        ) {
            return true
        }
        if (avatar.details.specific) {
            if (!isSpecific(avatar.details.index)) {
                return true
            }
        }
        val coord = avatar.details.currentCoord
        if (!worldEntityInfo.isVisible(
                this.localPlayerCurrentCoord,
                coord,
                max(renderDistance, avatar.details.renderDistance),
            )
        ) {
            return true
        }
        val filter = this.filter
        return filter != null &&
            !filter.accept(localPlayerIndex, avatar.details.index)
    }

    /**
     * Processes the NPCs that are in low resolution by requesting an iterator of NPC indices
     * within [renderDistance] of the local player's current coordinate.
     * This function is responsible for deciding which NPCs to move to high resolution,
     * and ignore which ones are already in high resolution. It is the server's duty to always
     * return all the NPCs that should be added, regardless of if they were previously already
     * added.
     * @param buffer the primary buffer into which to write the bitcode information
     */
    private fun processLowResolution(
        details: NpcInfoWorldDetails,
        buffer: BitBuf,
    ) {
        val lowCap = details.lowPriorityCap
        val normalSoftCap = details.normalPrioritySoftCap
        // If our local view is already maxed out, don't even bother calculating the below
        if (details.normalPriorityCount >= normalSoftCap &&
            details.lowPriorityCount >= lowCap
        ) {
            return
        }
        val worldEntityInfo =
            checkNotNull(this.worldEntityInfo) {
                "World entity info is null"
            }
        val world = worldEntityInfo.getAvatar(details.worldId) ?: return
        val encoder = lowResolutionToHighResolutionEncoders[oldSchoolClientType]
        val largeDistance = max(world.sizeX, world.sizeZ) > 3
        if (largeDistance) {
            details.largeUpdate = true
        }
        val coord = localPlayerCurrentCoord
        // Prefer player's current level if we're updating the world on which they stand
        val level =
            if (worldEntityInfo.getWorldEntity(coord) == details.worldId) {
                coord.level
            } else {
                world.activeLevel
            }
        val startX = world.southWestZoneX
        val startZ = world.southWestZoneZ
        val swCoord = CoordGrid(level, startX shl 3, startZ shl 3)
        val endX = world.southWestZoneX + world.sizeX
        val endZ = world.southWestZoneZ + world.sizeZ
        val filter = this.filter
        loop@for (x in startX..<endX) {
            for (z in startZ..<endZ) {
                val npcs = this.zoneIndexStorage.get(level, x, z) ?: continue
                for (k in 0..<npcs.size) {
                    val index = npcs[k].toInt() and NPC_INFO_CAPACITY
                    if (index == NPC_INFO_CAPACITY) {
                        break
                    }
                    if (isHighResolution(details, index)) {
                        continue
                    }
                    val avatar = repository.getOrNull(index) ?: continue
                    if (avatar.details.inaccessible) {
                        continue
                    }
                    if (avatar.details.priorityBitcode and AVATAR_NORMAL_PRIORITY_FLAG != 0) {
                        // For normal priority, once both our groups are capped out, we just break out of the loop,
                        // as neither low nor normal priority NPCs can be added now.
                        if (details.normalPriorityCount >= normalSoftCap && details.lowPriorityCount >= lowCap) {
                            break@loop
                        }
                    } else {
                        // For low priority, if we've reached our cap, just move on - there might be normal
                        // priority NPCs still coming.
                        if (details.lowPriorityCount >= lowCap) {
                            continue
                        }
                    }
                    if (avatar.details.specific) {
                        if (!isSpecific(index)) {
                            continue
                        }
                    }
                    if (filter != null && !filter.accept(localPlayerIndex, index)) {
                        continue
                    }
                    avatar.addObserver(localPlayerIndex)
                    val i = details.highResolutionNpcIndexCount++
                    details.incrementPriority(
                        i,
                        avatar.details.priorityBitcode and AVATAR_NORMAL_PRIORITY_FLAG == 0,
                    )
                    details.highResolutionNpcIndices[i] = index.toUShort()
                    val observerFlags = avatar.extendedInfo.getLowToHighResChangeExtendedInfoFlags()
                    if (observerFlags != 0) {
                        details.observerExtendedInfoFlags.addFlag(details.extendedInfoCount, observerFlags)
                    }
                    val extendedInfo = (avatar.extendedInfo.flags or observerFlags) != 0
                    if (extendedInfo) {
                        details.extendedInfoIndices[details.extendedInfoCount++] = index.toUShort()
                    }
                    encoder.encode(
                        buffer,
                        avatar.details,
                        extendedInfo,
                        swCoord,
                        largeDistance,
                        NpcInfoProtocol.cycleCount,
                    )
                }
            }
        }
    }

    private fun processRootWorldLowResolution(
        details: NpcInfoWorldDetails,
        buffer: BitBuf,
        renderDistance: Int,
        zoneSearchRadius: Int,
    ) {
        val lowCap = details.lowPriorityCap
        val normalSoftCap = details.normalPrioritySoftCap
        // If our local view is already maxed out, don't even bother calculating the below
        if (details.normalPriorityCount >= normalSoftCap &&
            details.lowPriorityCount >= lowCap ||
            zoneSearchRadius < 0
        ) {
            return
        }
        val worldEntityInfo =
            checkNotNull(this.worldEntityInfo) {
                "World entity info is null"
            }
        val encoder = lowResolutionToHighResolutionEncoders[oldSchoolClientType]
        val largeDistance = zoneSearchRadius > 3
        if (largeDistance) {
            details.largeUpdate = true
        }
        val rootWorldCoord = worldEntityInfo.getCoordGridInRootWorld(localPlayerCurrentCoord)
        val centerX = rootWorldCoord.x
        val centerZ = rootWorldCoord.z
        val level = rootWorldCoord.level
        val startX = ((centerX shr 3) - zoneSearchRadius).coerceAtLeast(0)
        val startZ = ((centerZ shr 3) - zoneSearchRadius).coerceAtLeast(0)
        val endX = ((centerX shr 3) + zoneSearchRadius).coerceAtMost(0x7FF)
        val endZ = ((centerZ shr 3) + zoneSearchRadius).coerceAtMost(0x7FF)
        val filter = this.filter
        loop@for (x in startX..endX) {
            for (z in startZ..endZ) {
                val npcs = this.zoneIndexStorage.get(level, x, z) ?: continue
                for (k in 0..<npcs.size) {
                    val index = npcs[k].toInt() and NPC_INFO_CAPACITY
                    if (index == NPC_INFO_CAPACITY) {
                        break
                    }
                    if (isHighResolution(details, index)) {
                        continue
                    }
                    val avatar = repository.getOrNull(index) ?: continue
                    if (avatar.details.inaccessible) {
                        continue
                    }
                    if (avatar.details.priorityBitcode and AVATAR_NORMAL_PRIORITY_FLAG != 0) {
                        // For normal priority, once both our groups are capped out, we just break out of the loop,
                        // as neither low nor normal priority NPCs can be added now.
                        if (details.normalPriorityCount >= normalSoftCap && details.lowPriorityCount >= lowCap) {
                            break@loop
                        }
                    } else {
                        // For low priority, if we've reached our cap, just move on - there might be normal
                        // priority NPCs still coming.
                        if (details.lowPriorityCount >= lowCap) {
                            continue
                        }
                    }
                    if (!worldEntityInfo.isVisibleInRoot(
                            rootWorldCoord,
                            avatar.details.currentCoord,
                            max(renderDistance, avatar.details.renderDistance),
                        )
                    ) {
                        continue
                    }
                    if (avatar.details.specific) {
                        if (!isSpecific(index)) {
                            continue
                        }
                    }
                    if (filter != null && !filter.accept(localPlayerIndex, index)) {
                        continue
                    }
                    avatar.addObserver(localPlayerIndex)
                    val i = details.highResolutionNpcIndexCount++
                    details.incrementPriority(
                        i,
                        avatar.details.priorityBitcode and AVATAR_NORMAL_PRIORITY_FLAG == 0,
                    )
                    details.highResolutionNpcIndices[i] = index.toUShort()
                    val observerFlags = avatar.extendedInfo.getLowToHighResChangeExtendedInfoFlags()
                    if (observerFlags != 0) {
                        details.observerExtendedInfoFlags.addFlag(details.extendedInfoCount, observerFlags)
                    }
                    val extendedInfo = (avatar.extendedInfo.flags or observerFlags) != 0
                    if (extendedInfo) {
                        details.extendedInfoIndices[details.extendedInfoCount++] = index.toUShort()
                    }
                    encoder.encode(
                        buffer,
                        avatar.details,
                        extendedInfo,
                        rootWorldCoord,
                        largeDistance,
                        NpcInfoProtocol.cycleCount,
                    )
                }
            }
        }
    }

    /**
     * Checks whether a npc by the index of [index] is already within our high resolution
     * view.
     * @param index the index of the npc to check
     * @return whether the npc at the given index is already in high resolution.
     */
    private fun isHighResolution(
        details: NpcInfoWorldDetails,
        index: Int,
    ): Boolean {
        // NOTE: Perhaps it's more efficient to just allocate 65535 bits and do a bit check?
        // Would cost ~16.76mb at max world capacity
        for (i in 0..<details.highResolutionNpcIndexCount) {
            if (details.highResolutionNpcIndices[i].toInt() == index) {
                return true
            }
        }
        return false
    }

    /**
     * This function allocates a new clean world details object,
     * as on reconnect, all existing npc info state is lost.
     * This function should be called on the old npc info object
     * whenever a reconnect occurs.
     */
    public fun onReconnect() {
        checkCommunicationThread()
        if (isDestroyed()) return
        releaseAllWorlds()
        // Restore the root world by polling a new one
        val details = detailsStorage.poll(ROOT_WORLD)
        this.details[WORLD_ENTITY_CAPACITY] = details
    }

    override fun onAlloc(
        index: Int,
        oldSchoolClientType: OldSchoolClientType,
        newInstance: Boolean,
    ) {
        checkCommunicationThread()
        this.localPlayerIndex = index
        this.oldSchoolClientType = oldSchoolClientType
        this.renderDistance = DEFAULT_DISTANCE
        this.localPlayerCurrentCoord = CoordGrid.INVALID
        this.localPlayerLastCoord = localPlayerCurrentCoord
        // There is always a root world!
        details[WORLD_ENTITY_CAPACITY] = detailsStorage.poll(ROOT_WORLD)
    }

    override fun onDealloc() {
        checkCommunicationThread()
        this.worldEntityInfo = null
        releaseAllWorlds()
    }

    private fun releaseAllWorlds() {
        for (index in this.details.indices) {
            val details = this.details[index] ?: continue
            releaseObservers(details)
            detailsStorage.push(details)
            this.details[index] = null
        }
    }

    private fun releaseObservers(details: NpcInfoWorldDetails) {
        for (i in 0..<details.highResolutionNpcIndexCount) {
            val npcIndex = details.highResolutionNpcIndices[i].toInt()
            val avatar = repository.getOrNull(npcIndex) ?: continue
            avatar.removeObserver(localPlayerIndex)
        }
    }

    public companion object {
        /**
         * The root world id, tracking the primary game map.
         */
        public const val ROOT_WORLD: Int = -1

        /**
         * The maximum number of dynamic world entities that can exist.
         */
        private const val WORLD_ENTITY_CAPACITY: Int = 2048

        /**
         * The default capacity of the backing byte buffer into which all npc info is written.
         */
        private const val BUF_CAPACITY: Int = 40_000

        /**
         * The default render distance for npcs.
         */
        private const val DEFAULT_DISTANCE: Int = 15

        /**
         * The default number of zones around the player's position to search for NPCs.
         * A radius of 3 implies it will search for entities that are up to at least 24 tiles away,
         * with the most being 31 tiles away (depending on player's positioning within their zone).
         */
        private const val DEFAULT_ZONE_SEARCH_RADIUS: Int = 3

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

        /**
         * Maximum unsigned short constant, the capacity of the npc info protocol.
         */
        private const val NPC_INFO_CAPACITY = 0xFFFF

        /**
         * The priority flag for normal priority NPCs.
         */
        private const val AVATAR_NORMAL_PRIORITY_FLAG: Int = 0x1

        private val logger = InlineLogger()
    }
}
