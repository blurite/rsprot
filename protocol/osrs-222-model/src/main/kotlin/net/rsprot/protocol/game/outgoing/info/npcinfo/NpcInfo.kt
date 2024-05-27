package net.rsprot.protocol.game.outgoing.info.npcinfo

import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufAllocator
import net.rsprot.buffer.bitbuffer.BitBuf
import net.rsprot.buffer.bitbuffer.toBitBuf
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprot.protocol.common.client.ClientTypeMap
import net.rsprot.protocol.common.client.OldSchoolClientType
import net.rsprot.protocol.common.game.outgoing.info.CoordGrid
import net.rsprot.protocol.common.game.outgoing.info.npcinfo.encoder.NpcResolutionChangeEncoder
import net.rsprot.protocol.game.outgoing.info.exceptions.InfoProcessException
import net.rsprot.protocol.game.outgoing.info.util.ReferencePooledObject
import net.rsprot.protocol.game.outgoing.info.worldentityinfo.WorldEntityAvatarRepository
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
 * @property oldSchoolClientType the client the player owning this npc info packet is on
 * @property localPlayerIndex the index of the local player that owns this npc info packet.
 * @property indexSupplier a supplier-style interface responsible for yielding npc indices
 * which are within vicinity of the player. This is the primary way the server will be providing
 * information about nearby NPCs to a player, as well as whether to render the NPC in the first place,
 * as some NPCs are meant to only render to a given player if certain conditions are met.
 * @property lowResolutionToHighResolutionEncoders a client map of low resolution to high resolution
 * change encoders, used to move a npc into high resolution for the given player.
 * As this is scrambled, a separate client-specific implementation is required.
 */
@Suppress("ReplaceUntilWithRangeUntil")
@ExperimentalUnsignedTypes
public class NpcInfo internal constructor(
    private val allocator: ByteBufAllocator,
    private val repository: NpcAvatarRepository,
    private var oldSchoolClientType: OldSchoolClientType,
    internal var localPlayerIndex: Int,
    private val indexSupplier: NpcIndexSupplier,
    private val lowResolutionToHighResolutionEncoders: ClientTypeMap<NpcResolutionChangeEncoder>,
    private val worldEntityAvatarRepository: WorldEntityAvatarRepository?,
) : ReferencePooledObject {
    /**
     * The maximum view distance how far a player will see other NPCs.
     * Unlike with player info, this does not automatically resize to accommodate for nearby NPCs,
     * as it is almost impossible for such a scenario to happen in the first place.
     * It is confirmed that OldSchool RuneScape does not do it either.
     */
    private var viewDistance: Int = MAX_SMALL_PACKET_DISTANCE

    /**
     * The exception that was caught during the processing of this player's npc info packet.
     * This exception will be propagated further during the [toNpcInfoPacket] function call,
     * allowing the server to handle it properly at a per-player basis.
     */
    internal var exception: Exception? = null

    /**
     * An array of world details, containing all the player info properties specific to a single world.
     * The root world is placed at the end of this array, however id -1 will be treated as the root.
     */
    internal val details: Array<NpcInfoWorldDetails?> = arrayOfNulls(WORLD_ENTITY_CAPACITY + 1)

    private var activeWorldId: Int = ROOT_WORLD

    private var renderCoord: CoordGrid = CoordGrid.INVALID

    init {
        // There is always a root world!
        details[WORLD_ENTITY_CAPACITY] = NpcInfoWorldDetails(ROOT_WORLD)
    }

    public fun setActiveWorld(worldId: Int) {
        require(worldId == ROOT_WORLD || worldId in 0..<WORLD_ENTITY_CAPACITY) {
            "World id must be -1 or in range of 0..<2048"
        }
        this.activeWorldId = worldId
    }

    public fun setRenderCoord(
        level: Int,
        x: Int,
        z: Int,
    ) {
        this.renderCoord = CoordGrid(level, x, z)
    }

    public fun resetRenderCoord() {
        this.renderCoord = CoordGrid.INVALID
    }

    public fun allocateWorld(worldId: Int) {
        require(worldId in 0..<WORLD_ENTITY_CAPACITY) {
            "World id out of bounds: $worldId"
        }
        val existing = details[worldId]
        require(existing == null) {
            "World $worldId already allocated."
        }
        details[worldId] = NpcInfoWorldDetails(worldId)
    }

    public fun destroyWorld(worldId: Int) {
        require(worldId in 0..<WORLD_ENTITY_CAPACITY) {
            "World id out of bounds: $worldId"
        }
        val existing = details[worldId]
        require(existing != null) {
            "World $worldId does not exist."
        }
        details[worldId] = null
    }

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
     * Returns the backing byte buffer holding all the computed information.
     * @throws IllegalStateException if the buffer is null, meaning it has no yet been
     * initialized for this cycle.
     */
    @Throws(IllegalStateException::class)
    public fun backingBuffer(worldId: Int): ByteBuf {
        return checkNotNull(getDetails(worldId).buffer)
    }

    /**
     * Returns the backing byte buffer holding all the computed information.
     * @throws IllegalStateException if the buffer is null, meaning it has no yet been
     * initialized for this cycle.
     */
    @Throws(IllegalStateException::class)
    private fun backingBuffer(details: NpcInfoWorldDetails): ByteBuf {
        return checkNotNull(details.buffer)
    }

    /**
     * Sets the view distance, meaning how far the player can see NPCs.
     * @param num the distance from which NPCs become visible
     */
    public fun setViewDistance(num: Int) {
        this.viewDistance = num
    }

    /**
     * Resets the view distance back to a default value of 15 tile radius.
     */
    public fun resetViewDistance() {
        this.viewDistance = MAX_SMALL_PACKET_DISTANCE
    }

    /**
     * Turns this npc info structure into a respective npc info packet, depending
     * on the current known view distance.
     */
    public fun toNpcInfoPacket(worldId: Int): OutgoingGameMessage {
        val exception = this.exception
        if (exception != null) {
            throw InfoProcessException(
                "Exception occurred during npc info processing for index $localPlayerIndex",
                exception,
            )
        }
        val details = getDetails(worldId)
        details.builtIntoPacket = true
        return if (this.viewDistance > MAX_SMALL_PACKET_DISTANCE) {
            NpcInfoLarge(backingBuffer(worldId))
        } else {
            NpcInfoSmall(backingBuffer(worldId))
        }
    }

    /**
     * Allocates a new buffer from the [allocator] with a capacity of [BUF_CAPACITY].
     * The old [NpcInfoWorldDetails.buffer] will not be released, as that is the duty of the encoder class.
     */
    private fun allocBuffer(worldId: Int): ByteBuf {
        // Acquire a new buffer with each cycle, in case the previous one isn't fully written out yet
        val buffer = allocator.buffer(BUF_CAPACITY, BUF_CAPACITY)
        val details = getDetails(worldId)
        details.buffer = buffer
        details.builtIntoPacket = false
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
        worldId: Int,
        level: Int,
        x: Int,
        z: Int,
    ) {
        val details = getDetails(worldId)
        details.localPlayerCurrentCoord =
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
    internal fun compute(details: NpcInfoWorldDetails) {
        val viewDistance = this.viewDistance
        val buffer = allocBuffer(details.worldId)
        buffer.toBitBuf().use { bitBuffer ->
            val fragmented = processHighResolution(details, bitBuffer, viewDistance)
            if (fragmented) {
                details.defragmentIndices()
            }
            processLowResolution(details, bitBuffer, viewDistance)
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
    public fun afterUpdate() {
        for (details in this.details) {
            if (details == null) {
                continue
            }
            details.localPlayerLastCoord = details.localPlayerCurrentCoord
            details.extendedInfoCount = 0
            details.observerExtendedInfoFlags.reset()
        }
    }

    /**
     * Writes the extended info blocks over to the backing buffer, based on the indices
     * of the NPCs from whom we requested extended info updates prior in this cycle.
     */
    internal fun putExtendedInfo(details: NpcInfoWorldDetails) {
        val jagBuffer = backingBuffer(details).toJagByteBuf()
        for (i in 0 until details.extendedInfoCount) {
            val index = details.extendedInfoIndices[i].toInt()
            val other = checkNotNull(repository.getOrNull(index))
            val observerFlag = other.extendedInfo.flags or details.observerExtendedInfoFlags.getFlag(i)
            other.extendedInfo.pExtendedInfo(
                oldSchoolClientType,
                jagBuffer,
                localPlayerIndex,
                details.extendedInfoCount - i,
                observerFlag,
            )
        }
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
        details: NpcInfoWorldDetails,
        buffer: BitBuf,
        viewDistance: Int,
    ): Boolean {
        // If no one to process, skip
        if (details.highResolutionNpcIndexCount == 0) {
            buffer.pBits(8, 0)
            return false
        }
        // If our coordinate compared to last cycle changed more than 'viewDistance'
        // tiles, every NPC in our local view would be removed anyhow,
        // so by sending the count as 0, client automatically removes everyone
        if (isTooFar(details, viewDistance)) {
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
                avatar.removeObserver()
            }
            details.highResolutionNpcIndexCount = 0
            return false
        }
        // Iterate NPCs in a backwards order until the first npc who should not be removed
        // everyone else will be automatically dropped off by the client if the count
        // transmitted is less than what the client currently knows about
        for (i in details.highResolutionNpcIndexCount - 1 downTo 0) {
            val npcIndex = details.highResolutionNpcIndices[i].toInt()
            val avatar = repository.getOrNull(npcIndex)
            if (!removeHighResolutionNpc(details, avatar, viewDistance)) {
                break
            }
            avatar?.removeObserver()
            details.highResolutionNpcIndexCount--
        }
        val processedCount = details.highResolutionNpcIndexCount
        buffer.pBits(8, processedCount)
        for (i in 0..<processedCount) {
            val npcIndex = details.highResolutionNpcIndices[i].toInt()
            val avatar = repository.getOrNull(npcIndex)
            if (removeHighResolutionNpc(details, avatar, viewDistance)) {
                buffer.pBits(1, 1)
                buffer.pBits(2, 3)
                avatar?.removeObserver()
                details.highResolutionNpcIndices[i] = NPC_INDEX_TERMINATOR
                details.highResolutionNpcIndexCount--
                continue
            }
            if (avatar.extendedInfo.flags != 0) {
                details.extendedInfoIndices[details.extendedInfoCount++] = npcIndex.toUShort()
            }
            val movementBuffer = checkNotNull(avatar.highResMovementBuffer)
            buffer.pBits(movementBuffer)
        }
        return processedCount != details.highResolutionNpcIndexCount
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
        details: NpcInfoWorldDetails,
        avatar: NpcAvatar?,
        viewDistance: Int,
    ): Boolean {
        contract {
            returns(false) implies (avatar != null)
        }
        if (avatar == null ||
            avatar.details.inaccessible ||
            avatar.details.isTeleporting()
        ) {
            return true
        }
        return !withinDistance(details.localPlayerCurrentCoord, avatar.details.currentCoord, viewDistance)
    }

    /**
     * Checks if the player has moved a greater distance from their previous coordinate
     * than the maximum [viewDistance], in which case all existing high resolution NPCs
     * can be removed in one go in a more efficient manner.
     * @param viewDistance the maximum view distance how far a player can see other npcs
     * @return whether the player has moved a greater distance than [viewDistance] since
     * the last cycle.
     */
    private fun isTooFar(
        details: NpcInfoWorldDetails,
        viewDistance: Int,
    ): Boolean {
        return !withinDistance(
            details.localPlayerLastCoord,
            details.localPlayerCurrentCoord,
            viewDistance,
        )
    }

    private fun isWorldVisible(details: NpcInfoWorldDetails): Boolean {
        if (details.worldId == ROOT_WORLD) {
            return false
        }
        val avatar =
            this.worldEntityAvatarRepository?.getOrNull(details.worldId)
                ?: return false
        return details.localPlayerCurrentCoord.inDistance(avatar.currentCoord, this.viewDistance) ||
            (renderCoord != CoordGrid.INVALID && renderCoord.inDistance(avatar.currentCoord, this.viewDistance))
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
        details: NpcInfoWorldDetails,
        buffer: BitBuf,
        viewDistance: Int,
    ) {
        // If our local view is already maxed out, don't even request for indices
        if (details.highResolutionNpcIndexCount >= MAX_HIGH_RESOLUTION_NPCS) {
            return
        }
        val encoder = lowResolutionToHighResolutionEncoders[oldSchoolClientType]
        val largeDistance = viewDistance > MAX_SMALL_PACKET_DISTANCE
        val npcs =
            this.indexSupplier.supply(
                localPlayerIndex,
                details.localPlayerCurrentCoord.level,
                details.localPlayerCurrentCoord.x,
                details.localPlayerCurrentCoord.z,
                viewDistance,
            )
        while (npcs.hasNext()) {
            val index = npcs.next() and NPC_INFO_CAPACITY
            if (index == NPC_INFO_CAPACITY || isHighResolution(details, index)) {
                continue
            }
            if (details.highResolutionNpcIndexCount >= MAX_HIGH_RESOLUTION_NPCS) {
                break
            }
            val avatar = repository.getOrNull(index) ?: continue
            if (avatar.details.inaccessible) {
                continue
            }
            avatar.addObserver()
            val i = details.highResolutionNpcIndexCount++
            details.highResolutionNpcIndices[i] = index.toUShort()
            val observerFlags = avatar.extendedInfo.getLowToHighResChangeExtendedInfoFlags()
            details.observerExtendedInfoFlags.addFlag(details.extendedInfoCount, observerFlags)
            val extendedInfo = (avatar.extendedInfo.flags or observerFlags) != 0
            if (extendedInfo) {
                details.extendedInfoIndices[details.extendedInfoCount++] = index.toUShort()
            }
            encoder.encode(
                buffer,
                avatar.details,
                extendedInfo,
                details.localPlayerCurrentCoord,
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
        oldSchoolClientType: OldSchoolClientType,
    ) {
        this.localPlayerIndex = index
        this.oldSchoolClientType = oldSchoolClientType
        this.viewDistance = MAX_SMALL_PACKET_DISTANCE
        for (details in this.details) {
            if (details == null) {
                continue
            }
            details.localPlayerLastCoord = CoordGrid.INVALID
            details.highResolutionNpcIndexCount = 0
            details.extendedInfoCount = 0
            details.observerExtendedInfoFlags.reset()
        }
    }

    override fun onDealloc() {
        for (details in this.details) {
            if (details == null) {
                continue
            }
            if (!details.builtIntoPacket) {
                val buffer = details.buffer
                if (buffer != null && buffer.refCnt() > 0) {
                    buffer.release(buffer.refCnt())
                }
            }
            details.buffer = null
        }
    }

    public companion object {
        public const val ROOT_WORLD: Int = -1
        private const val WORLD_ENTITY_CAPACITY: Int = 2048

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
