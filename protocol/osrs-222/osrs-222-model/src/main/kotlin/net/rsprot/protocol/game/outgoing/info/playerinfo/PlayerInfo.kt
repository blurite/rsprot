package net.rsprot.protocol.game.outgoing.info.playerinfo

import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufAllocator
import net.rsprot.buffer.bitbuffer.BitBuf
import net.rsprot.buffer.bitbuffer.UnsafeLongBackedBitBuf
import net.rsprot.buffer.bitbuffer.toBitBuf
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprot.protocol.common.client.OldSchoolClientType
import net.rsprot.protocol.game.outgoing.info.ByteBufRecycler
import net.rsprot.protocol.game.outgoing.info.ObserverExtendedInfoFlags
import net.rsprot.protocol.game.outgoing.info.exceptions.InfoProcessException
import net.rsprot.protocol.game.outgoing.info.playerinfo.PlayerInfoProtocol.Companion.PROTOCOL_CAPACITY
import net.rsprot.protocol.game.outgoing.info.playerinfo.util.CellOpcodes
import net.rsprot.protocol.game.outgoing.info.util.Avatar
import net.rsprot.protocol.game.outgoing.info.util.BuildArea
import net.rsprot.protocol.game.outgoing.info.util.ReferencePooledObject
import net.rsprot.protocol.internal.game.outgoing.info.CoordGrid
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.math.abs

/**
 * An implementation of the player info packet.
 * This class is responsible for tracking and building the packets each cycle.
 * This class utilizes [ReferencePooledObject], meaning instances of it will be pooled
 * and re-used as needed, as the data stored within them is relatively memory-heavy.
 *
 * @param protocol the repository of all the [PlayerInfo] objects,
 * as well as a source global information about everyone in the game.
 * As the packet is responsible for tracking everyone in the game,
 * we need to provide access to this.
 * @param localIndex the index of this local player. The index corresponds to the player's slot
 * in the world. The index will not change throughout the lifespan of a player,
 * but can change within allocations in the reference pool.
 * @param allocator the [ByteBuf] allocator responsible for allocating the primary buffer
 * the is written out to the pipeline, as well as any intermediate buffers used by extended
 * info blocks. The allocator should ideally be pooled, as we acquire a new instance with each
 * cycle. This is because there isn't necessarily a guarantee that Netty threads have fully
 * written the information out to the network by the time the next cycle comes along and starts
 * writing into this buffer. A direct implementation is also preferred, as this avoids unnecessary
 * copying from and to the heap.
 * @param oldSchoolClientType the client on which the player is logging into. This is utilized
 * to determine what encoders to use for extended info blocks.
 */
@Suppress("DuplicatedCode", "ReplaceUntilWithRangeUntil")
public class PlayerInfo internal constructor(
    private val protocol: PlayerInfoProtocol,
    internal var localIndex: Int,
    internal val allocator: ByteBufAllocator,
    private var oldSchoolClientType: OldSchoolClientType,
    public val avatar: PlayerAvatar,
    private val recycler: ByteBufRecycler = ByteBufRecycler(),
    private val globalLowResolutionPositionRepository: GlobalLowResolutionPositionRepository,
) : ReferencePooledObject {
    /**
     * The observer info flags are used for us to track extended info blocks which weren't necessarily
     * flagged on the target player. This can happen during the transitioning from low resolution
     * to high resolution, in which case appearance, move speed and face pathingentity may be transmitted,
     * despite not having been flagged. Additionally, some extended info blocks, such as hits and tinting,
     * will sometimes be observer-dependent. This means each observer will receive a different variant
     * of the extended info buffer. A simple example of this is the red circle hitmark ironmen will
     * see on NPCs whenever they attack a NPC that has already received damage from another player.
     * Only the ironman will receive information about that hitmark in this case, and no one else.
     */
    internal val observerExtendedInfoFlags: ObserverExtendedInfoFlags = ObserverExtendedInfoFlags(PROTOCOL_CAPACITY)

    /**
     * High resolution bit buffers are cached to avoid small computations for each observer,
     * and it allows us to reduce the number of [BitBuf.pBits] calls, which are quite expensive.
     * This implementation will store all the information inside a 'long' primitive, as the maximum
     * data size will always fit in under 50 bits.
     */
    private var highResMovementBuffer: UnsafeLongBackedBitBuf? = null

    /**
     * The exception that was caught during the processing of this player's playerinfo packet.
     * This exception will be propagated further during the [toPacket] function call,
     * allowing the server to handle it properly at a per-player basis.
     */
    @Volatile
    internal var exception: Exception? = null

    /**
     * An array of world details, containing all the player info properties specific to a single world.
     * The root world is placed at the end of this array, however id -1 will be treated as the root.
     */
    internal val details: Array<PlayerInfoWorldDetails?> = arrayOfNulls(PROTOCOL_CAPACITY + 1)

    /**
     * The currently active world id in which the local player resides.
     * We must know this as the local player is placed in every world that is rendered to them,
     * so implicitly, extended info blocks would be sent for every variant.
     * This results in unwanted duplication. With this active world value, we can filter local player's
     * extended info to only send for the world in which the player currently resides.
     */
    private var activeWorldId: Int = ROOT_WORLD

    /**
     * Whether to invalidate appearance cache.
     * This will be true only if a new world is being created, as the client keeps track of it per
     * world basis.
     */
    private var invalidateAppearanceCache: Boolean = false

    override fun isDestroyed(): Boolean = this.exception != null

    /**
     * Checks if appearance needs invalidation, and invalidates if so.
     */
    internal fun checkAppearanceInvalidation() {
        if (!invalidateAppearanceCache) {
            return
        }
        invalidateAppearanceCache = false
        avatar.extendedInfo.invalidateAppearanceCache()
    }

    /**
     * Sets an active world in which the player currently resides.
     * @param worldId the world id in which the player resides. A value of -1 implies
     * the root world, which is also the default. If the player moves onto one of the
     * dynamic worlds, this value must be updated to reflect on it.
     */
    public fun setActiveWorld(worldId: Int) {
        if (isDestroyed()) return
        require(worldId == ROOT_WORLD || worldId in 0..<2048) {
            "World id must be -1 or in range of 0..<2048"
        }
        this.activeWorldId = worldId
    }

    /**
     * Updates the render coordinate for the provided world id.
     * This coordinate is what will be used to perform distance checks between the player
     * and everyone else.
     * @param worldId the id of the world in which the player resides
     * @param level the height level at which the player resides
     * @param x the absolute x coordinate where the player resides
     * @param z the absolute z coordinate where the player resides
     */
    public fun updateRenderCoord(
        worldId: Int,
        level: Int,
        x: Int,
        z: Int,
    ) {
        if (isDestroyed()) return
        require(worldId == ROOT_WORLD || worldId in 0..<2048) {
            "World id must be -1 or in range of 0..<2048"
        }
        val details = getDetails(worldId)
        details.renderCoord = CoordGrid(level, x, z)
    }

    /**
     * Updates the build area of a given world to the specified one.
     * This will ensure that no players outside of this box will be
     * added to high resolution view.
     * @param worldId the id of the world to set the build area of,
     * with -1 being the root world.
     * @param buildArea the build area to assign.
     */
    public fun updateBuildArea(
        worldId: Int,
        buildArea: BuildArea,
    ) {
        if (isDestroyed()) return
        require(worldId == ROOT_WORLD || worldId in 0..<2048) {
            "World id must be -1 or in range of 0..<2048"
        }
        val details = getDetails(worldId)
        details.buildArea = buildArea
    }

    /**
     * Updates the build area of a given world to the specified one.
     * This will ensure that no players outside of this box will be
     * added to high resolution view.
     * @param worldId the id of the world to set the build area of,
     * with -1 being the root world.
     * @param zoneX the south-western zone x coordinate of the build area
     * @param zoneZ the south-western zone z coordinate of the build area
     * @param widthInZones the build area width in zones (typically 13, meaning 104 tiles)
     * @param heightInZones the build area height in zones (typically 13, meaning 104 tiles)
     */
    @JvmOverloads
    public fun updateBuildArea(
        worldId: Int,
        zoneX: Int,
        zoneZ: Int,
        widthInZones: Int = BuildArea.DEFAULT_BUILD_AREA_SIZE,
        heightInZones: Int = BuildArea.DEFAULT_BUILD_AREA_SIZE,
    ) {
        if (isDestroyed()) return
        require(worldId == ROOT_WORLD || worldId in 0..<2048) {
            "World id must be -1 or in range of 0..<2048"
        }
        val details = getDetails(worldId)
        details.buildArea = BuildArea(zoneX, zoneZ, widthInZones, heightInZones)
    }

    /**
     * Allocates a new player info tracking object for the respective [worldId],
     * keeping track of everyone that's within this new world entity.
     * @param worldId the new world entity id
     */
    public fun allocateWorld(worldId: Int) {
        if (isDestroyed()) return
        require(worldId in 0..<PROTOCOL_CAPACITY) {
            "World id out of bounds: $worldId"
        }
        val existing = details[worldId]
        require(existing == null) {
            "World $worldId already allocated."
        }
        details[worldId] = protocol.detailsStorage.poll(worldId)
        this.invalidateAppearanceCache = true
    }

    /**
     * Destroys player info tracking for the specified [worldId].
     * This is intended to be used when one of the world entities leaves the render distance.
     */
    public fun destroyWorld(worldId: Int) {
        if (isDestroyed()) return
        require(worldId in 0..<PROTOCOL_CAPACITY) {
            "World id out of bounds: $worldId"
        }
        val existing = details[worldId]
        require(existing != null) {
            "World $worldId does not exist."
        }
        details[worldId] = null
        protocol.detailsStorage.push(existing)
    }

    /**
     * Gets the world details implementation of the specified [worldId].
     */
    private fun getDetails(worldId: Int): PlayerInfoWorldDetails {
        val details =
            if (worldId == ROOT_WORLD) {
                details[PROTOCOL_CAPACITY]
            } else {
                require(worldId in 0..<PROTOCOL_CAPACITY) {
                    "World id out of bounds: $worldId"
                }
                details[worldId]
            }
        return checkNotNull(details) {
            "World info details not allocated for world $worldId"
        }
    }

    /**
     * Gets the world details implementation of the specified [worldId], or null if the
     * world has not been allocated.
     */
    private fun getDetailsOrNull(worldId: Int): PlayerInfoWorldDetails? =
        if (worldId == ROOT_WORLD) {
            details[PROTOCOL_CAPACITY]
        } else {
            details.getOrNull(worldId)
        }

    /**
     * Returns the backing buffer for this cycle, for the specified world details.
     * @throws IllegalStateException if the buffer has not been allocated yet.
     */
    @Throws(IllegalStateException::class)
    private fun backingBuffer(details: PlayerInfoWorldDetails): ByteBuf = checkNotNull(details.buffer)

    /**
     * Gets the high resolution indices of the given [worldId] in a new arraylist of integers.
     * The list is initialized to an initial capacity equal to the high resolution player index count.
     * @param worldId the worldId to collect the indices from. For root world, use [ROOT_WORLD].
     * @throws IllegalArgumentException if the world id is not in range of 0..<2048, or [ROOT_WORLD].
     * @throws IllegalStateException if the provided world has not been allocated. It is up to the
     * caller to ensure the world they're accessible is available. Root world will always be available
     * as long as the given info object is allocated.
     * @return the newly created arraylist of indices
     */
    public fun getHighResolutionIndices(worldId: Int): ArrayList<Int> {
        if (isDestroyed()) return ArrayList(0)
        val details = getDetails(worldId)
        val collection = ArrayList<Int>(details.highResolutionCount)
        for (i in 0..<details.highResolutionCount) {
            val index = details.highResolutionIndices[i].toInt()
            collection.add(index)
        }
        return collection
    }

    /**
     * Gets the high resolution indices of the given [worldId] in a new arraylist of integers, or null
     * if the provided world does not exist.
     * The list is initialized to an initial capacity equal to the high resolution player index count.
     * @param worldId the worldId to collect the indices from. For root world, use [ROOT_WORLD].
     * @throws IllegalArgumentException if the world id is not in range of 0..<2048, or [ROOT_WORLD].
     * @throws IllegalStateException if the provided world has not been allocated. It is up to the
     * caller to ensure the world they're accessible is available. Root world will always be available
     * as long as the given info object is allocated.
     * @return the newly created arraylist of indices, or null if the world does not exist.
     */
    public fun getHighResolutionIndicesOrNull(worldId: Int): ArrayList<Int>? {
        val details = getDetailsOrNull(worldId) ?: return null
        val collection = ArrayList<Int>(details.highResolutionCount)
        for (i in 0..<details.highResolutionCount) {
            val index = details.highResolutionIndices[i].toInt()
            collection.add(index)
        }
        return collection
    }

    /**
     * Appends the high resolution indices of the given [worldId] to the provided
     * [collection]. This can be used to determine which players the player is currently
     * seeing in the client.
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
        if (isDestroyed()) return collection
        val details =
            if (throwExceptionIfNoWorld) {
                getDetails(worldId)
            } else {
                getDetailsOrNull(worldId) ?: return collection
            }
        for (i in 0..<details.highResolutionCount) {
            val index = details.highResolutionIndices[i].toInt()
            collection.add(index)
        }
        return collection
    }

    /**
     * Turns the player info object into a wrapped packet.
     * This is necessary because the encoder itself is only triggered in Netty, and it is possible
     * that the buffer has already been replaced with a new variant before it gets to that stage.
     * @return thread-safe player info packet class, wrapping the pre-built buffer.
     * @throws InfoProcessException if there was an exception during the computation of player info
     * for this specific playerinfo object,
     */
    public fun toPacket(worldId: Int): PlayerInfoPacket {
        val details = getDetails(worldId)
        val exception = this.exception
        if (exception != null) {
            throw InfoProcessException(
                "Exception occurred during player info processing for index $localIndex",
                exception,
            )
        }
        return PlayerInfoPacket(backingBuffer(details))
    }

    /**
     * Updates the current known coordinate of the given [Avatar].
     * This function must be called on each avatar before player info is computed.
     * @param level the current height level of the avatar.
     * @param x the x coordinate of the avatar.
     * @param z the z coordinate of the avatar (this is commonly referred to as 'y' coordinate).
     * @throws IllegalArgumentException if [level] is not in range of 0 until 4, or [x]/[z] are
     * not in range of 0 until 16384.
     */
    @Throws(IllegalArgumentException::class)
    public fun updateCoord(
        level: Int,
        x: Int,
        z: Int,
    ) {
        if (isDestroyed()) return
        this.avatar.updateCoord(level, x, z)
    }

    /**
     * Checks whether the player at [index] is currently among high resolution players.
     * @param highResolutionPlayers a bitpacked long array containing boolean-type information.
     * @param index the index of the player to check.
     */
    private fun isHighResolution(
        highResolutionPlayers: LongArray,
        index: Int,
    ): Boolean {
        val longIndex = index ushr 6
        val bit = 1L shl (index and 0x3F)
        return highResolutionPlayers[longIndex] and bit != 0L
    }

    /**
     * Marks the player at index [index] as being in high resolution.
     * @param highResolutionPlayers a bitpacked long array containing boolean-type information.
     * @param index the index of the player to mark as high resolution.
     */
    private fun setHighResolution(
        highResolutionPlayers: LongArray,
        index: Int,
    ) {
        val longIndex = index ushr 6
        val bit = 1L shl (index and 0x3F)
        val cur = highResolutionPlayers[longIndex]
        highResolutionPlayers[longIndex] = cur or bit
    }

    /**
     * Marks the player at index [index] as being in low resolution.
     * @param highResolutionPlayers a bitpacked long array containing boolean-type information.
     * @param index the index of the player to mark as low resolution.
     */
    private fun unsetHighResolution(
        highResolutionPlayers: LongArray,
        index: Int,
    ) {
        val longIndex = index ushr 6
        val bit = 1L shl (index and 0x3F)
        val cur = highResolutionPlayers[longIndex]
        highResolutionPlayers[longIndex] = cur and bit.inv()
    }

    /**
     * Checks whether the player at [index] is currently among high resolution extended info players.
     * @param highResolutionExtendedInfoTrackedPlayers a bitpacked long array containing boolean-type information.
     * @param index the index of the player to check.
     */
    private fun isHighResolutionExtendedInfoTracked(
        highResolutionExtendedInfoTrackedPlayers: LongArray,
        index: Int,
    ): Boolean {
        val longIndex = index ushr 6
        val bit = 1L shl (index and 0x3F)
        return highResolutionExtendedInfoTrackedPlayers[longIndex] and bit != 0L
    }

    /**
     * Marks the player at index [index] as being in high resolution extended info.
     * @param highResolutionExtendedInfoTrackedPlayers a bitpacked long array containing boolean-type information.
     * @param index the index of the player to mark as high resolution.
     */
    private fun setHighResolutionExtendedInfoTracked(
        highResolutionExtendedInfoTrackedPlayers: LongArray,
        index: Int,
    ) {
        val longIndex = index ushr 6
        val bit = 1L shl (index and 0x3F)
        val cur = highResolutionExtendedInfoTrackedPlayers[longIndex]
        highResolutionExtendedInfoTrackedPlayers[longIndex] = cur or bit
    }

    /**
     * Marks the player at index [index] as being in low resolution extended info.
     * @param highResolutionExtendedInfoTrackedPlayers a bitpacked long array containing boolean-type information.
     * @param index the index of the player to mark as low resolution.
     */
    private fun unsetHighResolutionExtendedInfoTracked(
        highResolutionExtendedInfoTrackedPlayers: LongArray,
        index: Int,
    ) {
        val longIndex = index ushr 6
        val bit = 1L shl (index and 0x3F)
        val cur = highResolutionExtendedInfoTrackedPlayers[longIndex]
        highResolutionExtendedInfoTrackedPlayers[longIndex] = cur and bit.inv()
    }

    /**
     * Handles initializing absolute player positions.
     * @param byteBuf the buffer into which the information will be written.
     */
    public fun handleAbsolutePlayerPositions(
        worldId: Int,
        byteBuf: ByteBuf,
    ) {
        if (isDestroyed()) return
        check(avatar.currentCoord != CoordGrid.INVALID) {
            "Avatar position must be updated via playerinfo#updateCoord before sending RebuildLogin/ReconnectOk."
        }
        val details = getDetails(worldId)
        byteBuf.toBitBuf().use { buffer ->
            buffer.pBits(30, avatar.currentCoord.packed)
            setHighResolution(details.highResolutionPlayers, localIndex)
            details.highResolutionIndices[details.highResolutionCount++] = localIndex.toShort()
            for (i in 1 until PROTOCOL_CAPACITY) {
                if (i == localIndex) {
                    continue
                }
                val lowResolutionPosition = protocol.getLowResolutionPosition(i)
                buffer.pBits(18, lowResolutionPosition.packed)
                details.lowResolutionIndices[details.lowResolutionCount++] = i.toShort()
            }
        }
    }

    /**
     * Resets any existing state and allocates a new clean root world.
     * All other worlds are lost.
     * Cached state should be re-assigned from the server as a result of this.
     */
    public fun onReconnect() {
        if (isDestroyed()) return
        reset()
        // Restore the root world by polling a new one
        val details = protocol.detailsStorage.poll(ROOT_WORLD)
        this.details[PROTOCOL_CAPACITY] = details
        details.initialized = true
        avatar.postUpdate()
        avatar.extendedInfo.onReconnect()
    }

    /**
     * Ensures that the state has been correctly reset and a reconnect packet can continue.
     * @throws IllegalStateException if the state has not fully been cleaned up.
     */
    internal fun ensureReconnectCalled() {
        if (!isCleanState()) {
            throw IllegalStateException(
                "In order to use LoginResponse.ReconnectOk packet, " +
                    "playerinfo#onReconnect, npcInfo#onReconnect " +
                    "and worldEntityInfo#onReconnect must be called!",
            )
        }
    }

    /**
     * Checks whether all the info has been reset for this packet, ensuring that
     * a reconnect packet can successfully be initialized.
     * @return whether all the state has been reset.
     */
    private fun isCleanState(): Boolean {
        val root = getDetailsOrNull(ROOT_WORLD) ?: return true
        return root.buffer == null &&
            highResMovementBuffer == null &&
            root.lowResolutionCount == 0 &&
            root.highResolutionCount == 0 &&
            root.extendedInfoCount == 0
    }

    /**
     * Clears all the entities for the provided [worldId]. This function is __only__ intended to be used
     * together with the [net.rsprot.protocol.game.outgoing.worldentity.ClearEntities] packet.
     * This packet should only be called before [PlayerInfoProtocol.update] has been called, otherwise
     * problems may arise.
     * @param worldId the world to clear, either [ROOT_WORLD] or a value from 0..<2048
     * If the world is [ROOT_WORLD], all worlds will be cleared.
     * If the world is in range of 0..<2048, only that specific world will be cleared.
     */
    public fun clearEntities(worldId: Int) {
        if (isDestroyed()) return
        require(worldId == ROOT_WORLD || worldId in 0..<2048) {
            "World id must be -1 or in range of 0..<2048"
        }
        // Only clear the details if calling for the root world
        if (worldId == ROOT_WORLD) {
            for (i in this.details.indices) {
                // Skip the root world, as that still needs to remain
                if (i == PROTOCOL_CAPACITY) {
                    continue
                }
                val world = this.details[i]
                if (world != null) {
                    this.details[i] = null
                }
            }
        }
    }

    /**
     * Precalculates all the bitcodes for this player, for high-resolution updates.
     * This function will be thread-safe relative to other players and can be calculated concurrently for all players.
     */
    internal fun prepareBitcodes() {
        this.avatar.extendedInfo.observedChatStorage
            .reset()
        this.highResMovementBuffer = prepareHighResMovement()
    }

    /**
     * Pre-computes extended info blocks for this player. Only extended info blocks
     * which were flagged during this cycle will be pre-computed, with any on-demand
     * extended info blocks excluded in pre-computations altogether.
     */
    internal fun precomputeExtendedInfo() {
        avatar.extendedInfo.precompute()
    }

    /**
     * Writes the extended info blocks of everyone who were marked
     * during [pBitcodes] to the [PlayerInfoWorldDetails.buffer]. This will utilize fast native memory copying for any
     * pre-computed extended info blocks. For any observer-dependent info blocks,
     * a new [ByteBuf] instance is allocated from the [allocator], which is then written
     * the information, followed by a fast native copy, which is further followed by releasing
     * this temporary buffer back. As mentioned before, it is highly suggested to use a pooled
     * implementation of the [allocator].
     * This function is thread-safe relative to other players and can be computed for all players
     * concurrently.
     */
    internal fun putExtendedInfo(details: PlayerInfoWorldDetails) {
        val jagBuffer = backingBuffer(details).toJagByteBuf()
        for (i in 0 until details.extendedInfoCount) {
            val index = details.extendedInfoIndices[i].toInt()
            val other = protocol.getPlayerInfo(index)
            // If other is null at this point, it means it was destroyed mid-processing at an earlier
            // stage. In order to avoid the issue escalating further by throwing errors for every player
            // that was in vicinity of the player that got destroyed, we simply write no-mask-update,
            // even though a mask update was requested at an earlier stage.
            // The next game tick, the player will be removed as the info is null, which is one of
            // the conditions for removing another player from tracking.
            if (other == null) {
                jagBuffer.p1(0)
                continue
            }
            val observerFlag = observerExtendedInfoFlags.getFlag(index)
            val tracked =
                other.avatar.extendedInfo.pExtendedInfo(
                    oldSchoolClientType,
                    jagBuffer,
                    observerFlag,
                    avatar.extendedInfo,
                    details.extendedInfoCount - i,
                )
            if (tracked) {
                setHighResolutionExtendedInfoTracked(
                    details.highResolutionExtendedInfoTrackedPlayers,
                    index,
                )
            }
        }
    }

    /**
     * Writes to the actual buffers the prepared bitcodes and extended information.
     * This function will be thread-safe relative to other players and can be calculated concurrently for all players.
     */
    internal fun pBitcodes(details: PlayerInfoWorldDetails) {
        avatar.resize(details.highResolutionCount)
        val buffer = allocBuffer(details)
        val bitBuf = buffer.toBitBuf()
        bitBuf.use { processHighResolution(details, it, skipStationary = true) }
        bitBuf.use { processHighResolution(details, it, skipStationary = false) }
        bitBuf.use { processLowResolution(details, it, skipStationary = false) }
        bitBuf.use { processLowResolution(details, it, skipStationary = true) }
    }

    /**
     * Processes low resolution updates for all the players who are currently
     * in our low resolution view.
     * @param buffer the buffer into which to write the bitcodes regarding each player.
     * @param skipStationary whether to skip any players who were marked as stationary last cycle.
     */
    private fun processLowResolution(
        details: PlayerInfoWorldDetails,
        buffer: BitBuf,
        skipStationary: Boolean,
    ) {
        var skips = -1
        for (i in 0 until details.lowResolutionCount) {
            val index = details.lowResolutionIndices[i].toInt()
            val wasStationary = details.stationary[index].toInt() and WAS_STATIONARY != 0
            if (skipStationary == wasStationary) {
                continue
            }
            val other = protocol.getPlayerInfo(index)
            val lowResolutionBuffer = globalLowResolutionPositionRepository.getBuffer(index)
            if (other == null) {
                if (lowResolutionBuffer != null) {
                    if (skips > -1) {
                        pStationary(buffer, skips)
                        skips = -1
                    }
                    buffer.pBits(1, 1)
                    buffer.pBits(lowResolutionBuffer)
                    continue
                }
                skips++
                details.stationary[index] = (details.stationary[index].toInt() or IS_STATIONARY).toByte()
                continue
            }
            val visible = shouldMoveToHighResolution(details, other)
            if (!visible && (!details.initialized || lowResolutionBuffer == null)) {
                skips++
                details.stationary[index] = (details.stationary[index].toInt() or IS_STATIONARY).toByte()
                continue
            }
            if (skips > -1) {
                pStationary(buffer, skips)
                skips = -1
            }
            if (!visible) {
                buffer.pBits(1, 1)
                buffer.pBits(lowResolutionBuffer!!)
                continue
            }
            pLowResToHighRes(details, buffer, other)
        }
        if (skips > -1) {
            pStationary(buffer, skips)
        }
    }

    /**
     * Writes a transition from low resolution to high resolution for the given player.
     * @param buffer the buffer into which to write the transition.
     * @param other the player who is being moved from low resolution to high resolution.
     */
    private fun pLowResToHighRes(
        details: PlayerInfoWorldDetails,
        buffer: BitBuf,
        other: PlayerInfo,
    ) {
        val index = other.localIndex
        // The above one-liner pBits is equal to this comment:
        // buffer.pBits(1, 1)
        // buffer.pBits(2, 0)
        buffer.pBits(3, 1 shl 2)
        val lowResBuf = globalLowResolutionPositionRepository.getBuffer(index)
        if (details.initialized && lowResBuf != null) {
            buffer.pBits(1, 1)
            buffer.pBits(lowResBuf)
        } else {
            buffer.pBits(1, 0)
        }
        val (_, x, z) = other.avatar.currentCoord

        buffer.pBits(13, x)
        buffer.pBits(13, z)

        // Get a flags of all the extended info blocks that are 'outdated' to us and must be sent again.
        val extraFlags =
            other.avatar.extendedInfo.getLowToHighResChangeExtendedInfoFlags(
                avatar.extendedInfo,
                oldSchoolClientType,
            )
        // Mark those flags as observer-dependent.
        observerExtendedInfoFlags.addFlag(index, extraFlags)
        details.stationary[index] = (details.stationary[index].toInt() or IS_STATIONARY).toByte()
        setHighResolution(details.highResolutionPlayers, index)
        val flag = other.avatar.extendedInfo.flags or observerExtendedInfoFlags.getFlag(index)
        val hasExtendedInfoBlock = flag != 0
        if (hasExtendedInfoBlock) {
            details.extendedInfoIndices[details.extendedInfoCount++] = index.toShort()
            buffer.pBits(1, 1)
        } else {
            setHighResolutionExtendedInfoTracked(
                details.highResolutionExtendedInfoTrackedPlayers,
                index,
            )
            buffer.pBits(1, 0)
        }
    }

    /**
     * Processes high resolution updates for all the players who are currently
     * in our high resolution view.
     * @param buffer the buffer into which to write the bitcodes regarding each player.
     * @param skipStationary whether to skip any players who were marked as stationary last cycle.
     */
    private fun processHighResolution(
        details: PlayerInfoWorldDetails,
        buffer: BitBuf,
        skipStationary: Boolean,
    ) {
        var skips = -1
        for (i in 0 until details.highResolutionCount) {
            val index = details.highResolutionIndices[i].toInt()
            val wasStationary = (details.stationary[index].toInt() and WAS_STATIONARY) != 0
            if (skipStationary == wasStationary) {
                continue
            }
            val other = protocol.getPlayerInfo(index)
            if (!shouldStayInHighResolution(details, other)) {
                if (skips > -1) {
                    pStationary(buffer, skips)
                    skips = -1
                }
                pHighToLowResChange(details, buffer, index)
                continue
            }

            // If we still haven't tracked extended info for them, re-try
            if (!isHighResolutionExtendedInfoTracked(details.highResolutionExtendedInfoTrackedPlayers, index)) {
                val extraFlags =
                    other.avatar.extendedInfo.getLowToHighResChangeExtendedInfoFlags(
                        avatar.extendedInfo,
                        oldSchoolClientType,
                    )
                observerExtendedInfoFlags.addFlag(index, extraFlags)
            }
            val flag = other.avatar.extendedInfo.flags or observerExtendedInfoFlags.getFlag(index)
            val hasExtendedInfoBlock =
                flag != 0 &&
                    (details.worldId == activeWorldId || index != localIndex)
            if (!hasExtendedInfoBlock) {
                setHighResolutionExtendedInfoTracked(details.highResolutionExtendedInfoTrackedPlayers, index)
            }
            val highResBuf = other.highResMovementBuffer
            val skipped = !hasExtendedInfoBlock && (!details.initialized || highResBuf == null)
            if (!skipped) {
                if (skips > -1) {
                    pStationary(buffer, skips)
                    skips = -1
                }
                pHighRes(details, buffer, index, hasExtendedInfoBlock, highResBuf)
                continue
            }
            skips++
            details.stationary[index] = (details.stationary[index].toInt() or IS_STATIONARY).toByte()
        }
        if (skips > -1) {
            pStationary(buffer, skips)
        }
    }

    /**
     * Writes the [count] of consecutive stationary players
     * using [run-length encoding](https://en.wikipedia.org/wiki/Run-length_encoding).
     * @param buffer the buffer into which to write the encoded count.
     * @param count the count of players that were skipped.
     * The actual number that is written will always be 1 less, as the client automatically
     * includes 1 in the total value through the presence of a stationary block in the first place.
     */
    private fun pStationary(
        buffer: BitBuf,
        count: Int,
    ) {
        // The below code is a branchless variant of this:
        // buffer.pBits(1, 0)
        // when {
        //     count == 0 -> buffer.pBits(2, 0)
        //     count <= 0x1F -> {
        //         buffer.pBits(2, 1)
        //         buffer.pBits(5, count)
        //     }
        //     count <= 0xFF -> {
        //         buffer.pBits(2, 2)
        //         buffer.pBits(8, count)
        //     }
        //     else -> {
        //         buffer.pBits(2, 3)
        //         buffer.pBits(11, count)
        //     }
        // }
        //
        // The branching causes a significant (~15-20%) performance loss in the extreme
        // end-case benchmarks, so it's best to eliminate it.

        // (Special thanks to Greg for figuring out the magic below!)
        // Positive signum the bits proceeding the 1st, 5th and 8th bit to give a value 1 - 3 to
        // represent > 0, > 31 and > 255 respectively.
        val lowerBits = (-count ushr 31)
        val higherBits = (-(count shr 5) ushr 31) + (-(count shr 8) ushr 31)
        val bitCountOpcode = lowerBits + higherBits
        val valueBitCount = (lowerBits * 5) + (higherBits * 3)
        buffer.pBits(3 + valueBitCount, count or (bitCountOpcode shl valueBitCount))
    }

    /**
     * Writes high resolution information about a player into the [buffer].
     * @param buffer the buffer into which to write the bitcodes.
     * @param index the index of the player whose information we are writing.
     * @param extendedInfo whether this player also had extended info block changes.
     * @param highResBuf the pre-computed bit buffer regarding this player's movement.
     */
    private fun pHighRes(
        details: PlayerInfoWorldDetails,
        buffer: BitBuf,
        index: Int,
        extendedInfo: Boolean,
        highResBuf: UnsafeLongBackedBitBuf?,
    ) {
        buffer.pBits(1, 1)
        if (extendedInfo) {
            details.extendedInfoIndices[details.extendedInfoCount++] = index.toShort()
            buffer.pBits(1, 1)
        } else {
            buffer.pBits(1, 0)
        }
        if (details.initialized && highResBuf != null) {
            buffer.pBits(highResBuf)
        } else {
            buffer.pBits(2, 0)
        }
    }

    /**
     * Writes a high resolution to low resolution change for the player.
     * @param buffer the buffer into which to write the bitcodes.
     * @param index the index of the player that is being moved to low resolution.
     */
    private fun pHighToLowResChange(
        details: PlayerInfoWorldDetails,
        buffer: BitBuf,
        index: Int,
    ) {
        unsetHighResolution(details.highResolutionPlayers, index)
        unsetHighResolutionExtendedInfoTracked(details.highResolutionExtendedInfoTrackedPlayers, index)
        // The one-liner pBits is equal to the below comment:
        // buffer.pBits(1, 1)
        // buffer.pBits(1, 0)
        // buffer.pBits(2, 0)
        buffer.pBits(4, 1 shl 3)
        val buf = globalLowResolutionPositionRepository.getBuffer(index)
        if (details.initialized && buf != null) {
            buffer.pBits(1, 1)
            buffer.pBits(buf)
        } else {
            buffer.pBits(1, 0)
        }
    }

    /**
     * Checks if [other] is visible to us considering our [PlayerAvatar.resizeRange].
     * This function utilizes experimental contracts to avoid an unnecessary null-check,
     * as if the function returns true, the parameter cannot ever be null.
     * @param other the player whom to check.
     * @return true if the other should be moved to low resolution.
     */
    @OptIn(ExperimentalContracts::class)
    private fun shouldStayInHighResolution(
        details: PlayerInfoWorldDetails,
        other: PlayerInfo?,
    ): Boolean {
        contract {
            returns(true) implies (other != null)
        }
        // If the avatar is no longer logged in, remove it
        if (other == null) {
            return false
        }
        // Do not add or remove local player
        if (other.localIndex == localIndex) {
            return true
        }
        if (other.avatar.hidden) {
            return false
        }
        // If the avatar was allocated on this cycle, ensure we remove (and potentially re-add later)
        // this avatar. This is due to someone logging out and another player taking the avatar the same
        // cycle - which would otherwise potentially go by unnoticed, with the client assuming nothing changed.
        if (other.avatar.allocateCycle == PlayerInfoProtocol.cycleCount) {
            return false
        }
        val coord = other.avatar.currentCoord
        if (!details.renderCoord.inDistance(coord, this.avatar.resizeRange)) {
            return false
        }
        val buildArea = details.buildArea
        return buildArea == BuildArea.INVALID || coord in buildArea
    }

    /**
     * Checks if [other] is visible to us considering our [PlayerAvatar.resizeRange].
     * This function utilizes experimental contracts to avoid an unnecessary null-check,
     * as if the function returns true, the parameter cannot ever be null.
     * @param other the player whom to check.
     * @return true if the other player should be moved to high resolution.
     */
    @OptIn(ExperimentalContracts::class)
    private fun shouldMoveToHighResolution(
        details: PlayerInfoWorldDetails,
        other: PlayerInfo?,
    ): Boolean {
        contract {
            returns(true) implies (other != null)
        }
        // If the avatar is no longer logged in, remove it
        if (other == null || other.localIndex == localIndex) {
            return false
        }
        if (other.avatar.hidden) {
            return false
        }
        val coord = other.avatar.currentCoord
        if (!details.renderCoord.inDistance(coord, this.avatar.resizeRange)) {
            return false
        }
        val buildArea = details.buildArea
        return buildArea == BuildArea.INVALID || coord in buildArea
    }

    /**
     * Allocates a new buffer from the [allocator] with a capacity of [BUF_CAPACITY].
     * The old [PlayerInfoWorldDetails.buffer] will not be released, as that is the duty of the encoder class.
     */
    private fun allocBuffer(details: PlayerInfoWorldDetails): ByteBuf {
        // Acquire a new buffer with each cycle, in case the previous one isn't fully written out yet
        val buffer = allocator.buffer(BUF_CAPACITY, BUF_CAPACITY)
        details.buffer = buffer
        recycler += buffer
        return buffer
    }

    /**
     * Reset any temporary properties from this cycle.
     */
    internal fun postUpdate(details: PlayerInfoWorldDetails) {
        details.lowResolutionCount = 0
        details.highResolutionCount = 0
        // Only need to reset the count here, the actual numbers don't matter.
        details.extendedInfoCount = 0
        for (i in 1 until PROTOCOL_CAPACITY) {
            details.initialized = true
            details.stationary[i] = (details.stationary[i].toInt() shr 1).toByte()
            if (isHighResolution(details.highResolutionPlayers, i)) {
                details.highResolutionIndices[details.highResolutionCount++] = i.toShort()
            } else {
                details.lowResolutionIndices[details.lowResolutionCount++] = i.toShort()
            }
        }
    }

    /**
     * Marks the player info object as initialized, allowing for any further coordinate changes to take effect.
     */
    public fun postRebuildLogin() {
        if (isDestroyed()) return
        val rootDetails = getDetails(ROOT_WORLD)
        rootDetails.initialized = true
        avatar.postUpdate()
    }

    /**
     * A function to reset non-world specific properties when a cycle finishes.
     */
    internal fun cycleComplete() {
        this.avatar.postUpdate()
        avatar.extendedInfo.postUpdate()
        observerExtendedInfoFlags.reset()
    }

    /**
     * Resets all the primitive properties of this class which can be lazy-reset.
     * We utilize lazy resetting here as there's no guarantee that a given [PlayerInfo]
     * object will ever be re-used. Due to the nature of soft references, it is possible
     * for the garbage collector to collect it when it truly needs it. In order to reduce processing
     * time, we skip resetting these properties on de-allocation.
     * @param index the index of the new player who will be utilizing this player info object.
     * @param oldSchoolClientType the client the new player is utilizing.
     */
    override fun onAlloc(
        index: Int,
        oldSchoolClientType: OldSchoolClientType,
        newInstance: Boolean,
    ) {
        this.localIndex = index
        avatar.extendedInfo.localIndex = index
        this.oldSchoolClientType = oldSchoolClientType
        avatar.reset()
        this.avatar.allocateCycle = PlayerInfoProtocol.cycleCount
        this.activeWorldId = ROOT_WORLD
        // There is always a root world!
        val rootDetails = protocol.detailsStorage.poll(ROOT_WORLD)
        details[PROTOCOL_CAPACITY] = rootDetails

        if (newInstance) return
        rootDetails.lowResolutionIndices.fill(0)
        rootDetails.lowResolutionCount = 0
        rootDetails.highResolutionIndices.fill(0)
        rootDetails.highResolutionCount = 0
        rootDetails.highResolutionPlayers.fill(0L)
        rootDetails.highResolutionExtendedInfoTrackedPlayers.fill(0L)
        rootDetails.extendedInfoCount = 0
        rootDetails.extendedInfoIndices.fill(0)
        rootDetails.stationary.fill(0)
        observerExtendedInfoFlags.reset()
    }

    /**
     * Clears any references to temporary buffers on de-allocation, as we don't want these
     * to stick around for extended periods of time. Any primitive properties will remain untouched.
     */
    override fun onDealloc() {
        reset()
        avatar.extendedInfo.reset()
    }

    private fun reset() {
        for (i in 0..PROTOCOL_CAPACITY) {
            val details = this.details[i] ?: continue
            details.buffer = null
            protocol.detailsStorage.push(details)
            this.details[i] = null
        }
        highResMovementBuffer = null
    }

    /**
     * Prepares the high resolution movement block by checking the player's absolute coordinate
     * differences.
     * @return unsafe long-backed bit buffer that encodes the information into a 'long' primitive,
     * rather than a real byte buffer, in order to reduce unnecessary computations.
     */
    private fun prepareHighResMovement(): UnsafeLongBackedBitBuf? {
        val oldCoord = avatar.lastCoord
        val newCoord = avatar.currentCoord
        if (oldCoord == newCoord) {
            return null
        }
        val buffer = UnsafeLongBackedBitBuf()
        val deltaX = newCoord.x - oldCoord.x
        val deltaZ = newCoord.z - oldCoord.z
        val deltaLevel = newCoord.level - oldCoord.level
        val absX = abs(deltaX)
        val absZ = abs(deltaZ)
        if (deltaLevel != 0 || absX > 2 || absZ > 2) {
            if (absX >= 16 || absZ >= 16) {
                pLargeTeleport(buffer, deltaX, deltaZ, deltaLevel)
            } else {
                pSmallTeleport(buffer, deltaX, deltaZ, deltaLevel)
            }
        } else if (absX == 2 || absZ == 2) {
            pRun(buffer, deltaX, deltaZ)
        } else {
            // Guaranteed to be walking here, as our 'oldCoord == newCoord' covers the stationary condition.
            pWalk(buffer, deltaX, deltaZ)
        }
        return buffer
    }

    /**
     * Writes a single cell movement bitcode.
     * @param buffer the buffer into which to write the bitcode.
     * @param deltaX the x-coordinate delta the player moved.
     * @param deltaZ the z-coordinate delta the player moved.
     * @throws ArrayIndexOutOfBoundsException if the provided deltas do not result in a
     * one-cell movement.
     */
    @Throws(ArrayIndexOutOfBoundsException::class)
    private fun pWalk(
        buffer: UnsafeLongBackedBitBuf,
        deltaX: Int,
        deltaZ: Int,
    ) {
        buffer.pBits(2, 1)
        buffer.pBits(3, CellOpcodes.singleCellMovementOpcode(deltaX, deltaZ))
    }

    /**
     * Writes a dual cell movement bitcode.
     * @param buffer the buffer into which to write the bitcode.
     * @param deltaX the x-coordinate delta the player moved.
     * @param deltaZ the z-coordinate delta the player moved.
     * @throws ArrayIndexOutOfBoundsException if the provided deltas do not result in a
     * dual-cell movement.
     */
    @Throws(ArrayIndexOutOfBoundsException::class)
    private fun pRun(
        buffer: UnsafeLongBackedBitBuf,
        deltaX: Int,
        deltaZ: Int,
    ) {
        buffer.pBits(2, 2)
        buffer.pBits(4, CellOpcodes.dualCellMovementOpcode(deltaX, deltaZ))
    }

    /**
     * Writes a low-distance movement block, capped to a maximum delta of 15 coordinates
     * as well as any level changes.
     * @param buffer the buffer into which to write the bitcode.
     * @param deltaX the x-coordinate delta the player moved.
     * @param deltaZ the z-coordinate delta the player moved.
     * @param deltaLevel the level-coordinate delta the player moved.
     */
    private fun pSmallTeleport(
        buffer: UnsafeLongBackedBitBuf,
        deltaX: Int,
        deltaZ: Int,
        deltaLevel: Int,
    ) {
        buffer.pBits(2, 3)
        buffer.pBits(1, 0)
        buffer.pBits(2, deltaLevel and 0x3)
        buffer.pBits(5, deltaX and 0x1F)
        buffer.pBits(5, deltaZ and 0x1F)
    }

    /**
     * Writes a long-distance movement block, completely uncapped for the game world.
     * @param buffer the buffer into which to write the bitcode.
     * @param deltaX the x-coordinate delta the player moved.
     * @param deltaZ the z-coordinate delta the player moved.
     * @param deltaLevel the level-coordinate delta the player moved.
     */
    private fun pLargeTeleport(
        buffer: UnsafeLongBackedBitBuf,
        deltaX: Int,
        deltaZ: Int,
        deltaLevel: Int,
    ) {
        buffer.pBits(2, 3)
        buffer.pBits(1, 1)
        buffer.pBits(2, deltaLevel and 0x3)
        buffer.pBits(14, deltaX and 0x3FFF)
        buffer.pBits(14, deltaZ and 0x3FFF)
    }

    public companion object {
        /**
         * The default capacity of the backing byte buffer into which all player info is written.
         */
        private const val BUF_CAPACITY: Int = 40_000

        /**
         * The flag indicating that a player was stationary in the previous cycle.
         */
        private const val WAS_STATIONARY: Int = 0x1

        /**
         * The flag indicating that a player is stationary in the current cycle.
         */
        private const val IS_STATIONARY: Int = 0x2

        /**
         * The constant id for the root world.
         */
        public const val ROOT_WORLD: Int = -1
    }
}
