package net.rsprot.protocol.game.outgoing.info.worldentityinfo

import com.github.michaelbull.logging.InlineLogger
import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufAllocator
import net.rsprot.buffer.JagByteBuf
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprot.protocol.common.client.OldSchoolClientType
import net.rsprot.protocol.game.outgoing.info.ByteBufRecycler
import net.rsprot.protocol.game.outgoing.info.exceptions.InfoProcessException
import net.rsprot.protocol.game.outgoing.info.util.BuildArea
import net.rsprot.protocol.game.outgoing.info.util.PacketResult
import net.rsprot.protocol.game.outgoing.info.util.ReferencePooledObject
import net.rsprot.protocol.internal.checkCommunicationThread
import net.rsprot.protocol.internal.game.outgoing.info.CoordFine
import net.rsprot.protocol.internal.game.outgoing.info.CoordGrid
import net.rsprot.protocol.internal.game.outgoing.info.util.ZoneIndexStorage
import java.util.Collections

/**
 * The world entity info class tracks everything about the world entities that
 * are near this player.
 * @property localIndex the index of the local player that owns this world entity info.
 * @property allocator the byte buffer allocator used to build the buffer for the packet.
 * @property oldSchoolClientType the client type on which the player has logged in.
 * @property avatarRepository the avatar repository keeping track of every known
 * world entity in the root world.
 * @property zoneIndexStorage the storage responsible for tracking the zones in which
 * the world entities currently lie.
 * @property renderDistance the render distance in tiles, effectively how far to render
 * world entities from the local player (or the camera pov)
 * @property coordInRootWorld the current real coordinate of the local player.
 * @property rootBuildArea the current build area of the player, this is the root base
 * map that's being rendered to the player.
 * @property highResolutionIndicesCount the number of high resolution world entity avatars.
 * @property highResolutionIndices the indices of all the high resolution avatars currently
 * being tracked.
 * @property temporaryHighResolutionIndices a temporary array of high resolution avatar indices,
 * allowing for more efficient defragmentation of the indices when indices get removed
 * from the middle of the array.
 * @property allWorldEntities the indices of all the world entities currently in high resolution,
 * provided in a list format as the server must know everything currently rendered, so it can
 * perform accurate updates to player info, npc info and zones.
 * @property addedWorldEntities the indices of all the world entities that were added within
 * this cycle after it has been computed. The server can use this information to build the
 * REBUILD_WORLDENTITY packet, which is used to actually render the instance itself.
 * @property removedWorldEntities the indices of all the world entities that were removed
 * within this cycle after it has been computed. These are only removed from the high resolution,
 * and not necessarily the world itself. This allows the server to inform NPC and player info
 * protocol to deallocate the instances.
 * @property buffer the buffer for this world entity info packet.
 * @property exception the exception that was caught during the computations
 * of this world entity info, if any. This will be thrown as the [toPacketResult]
 * function is called, allowing the server to handle it from the correct
 * perspective of the caller, as the protocol itself is computed in for the
 * entire server in one go.
 */
@Suppress("MemberVisibilityCanBePrivate", "DuplicatedCode", "EmptyRange")
public class WorldEntityInfo internal constructor(
    internal var localIndex: Int,
    internal val allocator: ByteBufAllocator,
    private var oldSchoolClientType: OldSchoolClientType,
    private val avatarRepository: WorldEntityAvatarRepository,
    private val zoneIndexStorage: ZoneIndexStorage,
    private val recycler: ByteBufRecycler = ByteBufRecycler(),
) : ReferencePooledObject {
    private var renderDistance: Int = DEFAULT_RENDER_DISTANCE
    private var zoneSeekRadius: Int = DEFAULT_ZONE_SEEK_RADIUS
    private var coordInRootWorld: CoordGrid = CoordGrid.INVALID
    private var rootBuildArea: BuildArea = BuildArea.INVALID
    private var highResolutionIndicesCount: Int = 0
    private var highResolutionIndices: ShortArray =
        ShortArray(WorldEntityProtocol.CAPACITY) {
            INDEX_TERMINATOR
        }
    private var temporaryHighResolutionIndices: ShortArray =
        ShortArray(WorldEntityProtocol.CAPACITY) {
            INDEX_TERMINATOR
        }
    private val unsortedTopKArray: WorldEntityUnsortedTopKArray = WorldEntityUnsortedTopKArray(MAX_HIGH_RES_COUNT)
    private val allWorldEntities = ArrayList<Int>()
    private val allWorldEntitiesUnmodifiable: List<Int> = Collections.unmodifiableList(allWorldEntities)
    private val addedWorldEntities = ArrayList<Int>()
    private val addedWorldEntitiesUnmodifiable: List<Int> = Collections.unmodifiableList(addedWorldEntities)
    private val removedWorldEntities = ArrayList<Int>()
    private val removedWorldEntitiesUnmodifiable: List<Int> = Collections.unmodifiableList(removedWorldEntities)
    private var buffer: ByteBuf? = null

    /**
     * The previous world entity info packet that was created.
     * We ensure that a server hasn't accidentally left a packet unwritten, which would
     * de-synchronize the client and cause errors.
     */
    internal var previousPacket: WorldEntityInfoV6Packet? = null

    @Volatile
    internal var exception: Exception? = null

    override fun isDestroyed(): Boolean = this.exception != null

    /**
     * Gets the world entity avatar with the provided [index].
     */
    internal fun getAvatar(index: Int): WorldEntityAvatar? {
        return avatarRepository.getOrNull(index)
    }

    /**
     * Updates the render distance for this player, potentially allowing
     * the world entities to be rendered from further away. All instances
     * will however still be constrained to within the build area, in their
     * entirety - if they cannot fulfill that constraint, they will not be
     * put into high resolution view.
     * @param distance the distance in tiles how far the world entities should
     * be rendered from the player (or the camera's POV)
     * @param zoneSeekRadius the radius in zones to search around the center point.
     * By default, it will match old school which divides the distance by 8 and floors.
     * This has a side effect of often searching a smaller distance than what it should.
     * For example, a distance of 31 requires a zone seek radius of 4 to fully satisfy
     * the distance, as the player can be in various positions within their current zone.
     */
    @JvmOverloads
    public fun updateRenderDistance(
        distance: Int,
        zoneSeekRadius: Int = distance ushr 3,
    ) {
        checkCommunicationThread()
        if (isDestroyed()) return
        this.renderDistance = distance
        this.zoneSeekRadius = zoneSeekRadius
    }

    /**
     * Updates the build area for this player. This should always perfectly correspond to
     * the actual build area that is sent via REBUILD_NORMAL or REBUILD_REGION packets.
     * @param buildArea the build area in which everything is rendered.
     */
    internal fun updateRootBuildArea(buildArea: BuildArea) {
        checkCommunicationThread()
        if (isDestroyed()) return
        this.rootBuildArea = buildArea
    }

    /**
     * Gets a list of all the world entity indices that are currently in high resolution,
     * allowing for correct functionality for player and npc infos, as well as zone updates.
     * @return a list of indices of the world entities currently in high resolution.
     */
    public fun getAllWorldEntityIndices(): List<Int> {
        if (isDestroyed()) return emptyList()
        return this.allWorldEntitiesUnmodifiable
    }

    /**
     * Gets the indices of all the world entities that were added to high resolution in this cycle,
     * allowing the server to allocate new player and npc info instances, and sync the state of the
     * zones in those world entities.
     * @return a list of all the world entity indices added to the high resolution view in this
     * cycle.
     */
    public fun getAddedWorldEntityIndices(): List<Int> {
        if (isDestroyed()) return emptyList()
        return this.addedWorldEntitiesUnmodifiable
    }

    /**
     * Gets the indices of all the world entities that were removed from the high resolution in
     * this cycle, allowing the server to destroy the player and npc info instances corresponding
     * to them, and to clear the zones that were being tracked due to it.
     * @return a list of all the indices of the world entities that were removed from the high
     * resolution view this cycle.
     */
    public fun getRemovedWorldEntityIndices(): List<Int> {
        if (isDestroyed()) return emptyList()
        return this.removedWorldEntitiesUnmodifiable
    }

    /**
     * Updates the current real absolute coordinate of the local player in the world.
     * @param coordGrid the root coordgrid of the player.
     */
    public fun updateRootCoord(coordGrid: CoordGrid) {
        checkCommunicationThread()
        if (isDestroyed()) return
        this.coordInRootWorld = coordGrid
    }

    /**
     * Returns the backing byte buffer for this world entity info instance.
     * @return the byte buffer instance into which all the world entity info
     * is being written.
     * @throws IllegalStateException if the buffer has not yet been allocated.
     */
    @Throws(IllegalStateException::class)
    public fun backingBuffer(): ByteBuf = checkNotNull(buffer)

    /**
     * Turns the previously-computed world entity info into a packet instance
     * which can be flushed to the client, or an exception if one was thrown while
     * building the packet.
     * @return the world entity packet instance in a [PacketResult].
     */
    internal fun toPacketResult(): PacketResult<WorldEntityInfoV6Packet> {
        val exception = this.exception
        if (exception != null) {
            return PacketResult.failure(
                InfoProcessException(
                    "Exception occurred during player info processing for index $localIndex",
                    exception,
                ),
            )
        }
        val previousPacket =
            previousPacket
                ?: return PacketResult.failure(
                    IllegalStateException("Previous world entity info packet not calculated."),
                )
        return PacketResult.success(previousPacket)
    }

    /**
     * Allocates a new buffer for the next world entity info packet.
     * Furthermore, resets some temporary properties from the last cycle.
     * @return the buffer into which everything is written about this packet.
     */
    private fun allocBuffer(): ByteBuf {
        // Acquire a new buffer with each cycle, in case the previous one isn't fully written out yet
        val buffer = allocator.buffer(BUF_CAPACITY, BUF_CAPACITY)
        this.buffer = buffer
        recycler += buffer
        this.addedWorldEntities.clear()
        this.removedWorldEntities.clear()
        return buffer
    }

    /**
     * Defragments the indices of the high resolution world entities.
     * This is done only if world entities were removed in the middle of
     * the array.
     */
    private fun defragmentIndices() {
        var count = 0
        for (i in highResolutionIndices.indices) {
            if (count >= highResolutionIndicesCount) {
                break
            }
            val index = highResolutionIndices[i]
            if (index != INDEX_TERMINATOR) {
                temporaryHighResolutionIndices[count++] = index
            }
        }
        val uncompressed = this.highResolutionIndices
        this.highResolutionIndices = this.temporaryHighResolutionIndices
        this.temporaryHighResolutionIndices = uncompressed
    }

    /**
     * Performs the full world entity info update for the given player.
     */
    internal fun updateWorldEntities() {
        val buffer = allocBuffer().toJagByteBuf()
        search()
        val fragmented = processHighResolution(buffer)
        if (fragmented) {
            defragmentIndices()
        }
        processLowResolution(buffer)
    }

    /**
     * Sets up the packet to be consumed with the next call.
     */
    internal fun postUpdate() {
        if (this.previousPacket?.isConsumed() == false) {
            logger.warn {
                "Previous world entity info packet was calculated but " +
                    "not sent out to the client for player index $localIndex!"
            }
        }
        val packet = WorldEntityInfoV6Packet(backingBuffer())
        this.previousPacket = packet
    }

    /**
     * Processes all the currently tracked high resolution world entities.
     * @param buffer the buffer into which to write the high resolution updates.
     * @return whether any world entities were removed from high resolution, meaning
     * a defragmentation process is necessary.
     */
    private fun processHighResolution(buffer: JagByteBuf): Boolean {
        val count = this.highResolutionIndicesCount

        // Iterate worlds in a backwards order until the first world which should not be removed
        // everyone else will be automatically dropped off by the client if the count
        // transmitted is less than what the client currently knows about
        for (i in count - 1 downTo 0) {
            val index = this.highResolutionIndices[i].toInt()
            val avatar = avatarRepository.getOrNull(index)
            val needsRemoving = avatar == null || !this.unsortedTopKArray.contains(index) || isReallocated(avatar)
            if (!needsRemoving) {
                break
            }

            highResolutionIndices[i] = INDEX_TERMINATOR
            this.highResolutionIndicesCount--
            this.removedWorldEntities += index
            allWorldEntities -= index
        }

        buffer.p1(this.highResolutionIndicesCount)
        for (i in 0..<this.highResolutionIndicesCount) {
            val index = this.highResolutionIndices[i].toInt()
            val avatar = avatarRepository.getOrNull(index)
            if (avatar == null || !this.unsortedTopKArray.contains(index) || isReallocated(avatar)) {
                highResolutionIndices[i] = INDEX_TERMINATOR
                this.highResolutionIndicesCount--
                this.removedWorldEntities += index
                allWorldEntities -= index
                buffer.p1(0)
                continue
            }
            val precomputedBuffer = checkNotNull(avatar.highResolutionBuffer)
            buffer.buffer.writeBytes(
                precomputedBuffer,
                precomputedBuffer.readerIndex(),
                precomputedBuffer.readableBytes(),
            )
            putWorldEntityExtendedInfo(avatar, buffer)
        }
        return count != this.highResolutionIndicesCount
    }

    /**
     * Performs a search to find the indices of the world entities that are the closest to the player,
     * while also prioritizing higher priority ones.
     */
    private fun search() {
        this.unsortedTopKArray.reset()
        val currentWorldEntityIndex = getWorldEntityIndex(this.coordInRootWorld)
        val coordFineInRootWorld = getCoordFineInRootWorld(this.coordInRootWorld)
        val level = coordFineInRootWorld.y
        val centerFineX = coordFineInRootWorld.x
        val centerFineZ = coordFineInRootWorld.z
        val startZoneX = ((centerFineX shr 10) - zoneSeekRadius).coerceAtLeast(0)
        val startZoneZ = ((centerFineZ shr 10) - zoneSeekRadius).coerceAtLeast(0)
        val endZoneX = ((centerFineX shr 10) + zoneSeekRadius).coerceAtMost(0x7FF)
        val endZoneZ = ((centerFineZ shr 10) + zoneSeekRadius).coerceAtMost(0x7FF)
        for (x in startZoneX..endZoneX) {
            for (z in startZoneZ..endZoneZ) {
                val npcs = this.zoneIndexStorage.get(level, x, z) ?: continue
                for (k in 0..<npcs.size) {
                    val index = npcs[k].toInt() and WORLDENTITY_LOOKUP_TERMINATOR
                    if (index == WORLDENTITY_LOOKUP_TERMINATOR) {
                        break
                    }
                    val avatar = avatarRepository.getOrNull(index) ?: continue
                    // Secondary build-area distance check
                    if (!inRange(avatar)) {
                        continue
                    }
                    val isCurrentWorldEntity = index == currentWorldEntityIndex
                    if (avatar.specific) {
                        // Skip the world if it is marked as specific, and we don't own it,
                        // nor are we currently residing on it.
                        if (!isCurrentWorldEntity && avatar.ownerIndex != localIndex) {
                            continue
                        }
                    }
                    val avatarCoord = avatar.currentCoordFine
                    val dx = (centerFineX - avatarCoord.x).toLong()
                    val dz = (centerFineZ - avatarCoord.z).toLong()
                    val distanceSquared = dx * dx + dz * dz
                    // We have to always consider the world on which the player currently is as
                    // the highest priority possible, to ensure that it renders that world no matter
                    // the circumstances. This is solely for the purpose of serverside transmission
                    // and client-side priority can differ and be as-is.
                    val priority =
                        if (isCurrentWorldEntity) {
                            WorldEntityPriority.LOCAL_PLAYER.id + 1
                        } else {
                            avatar.priorityTowards(localIndex).id
                        }
                    this.unsortedTopKArray.offer(index, (priority.toLong() shl 60) - distanceSquared)
                }
            }
        }
    }

    /**
     * Processes the low resolution update for this world entity info,
     * adding any currently untracked world entities which are close enough
     * to high resolution.
     * @param buffer the buffer into which to write the updates.
     */
    private fun processLowResolution(buffer: JagByteBuf) {
        if (this.highResolutionIndicesCount >= MAX_HIGH_RES_COUNT) {
            return
        }
        val topKArray = this.unsortedTopKArray
        val indices = topKArray.indices
        val length = topKArray.size
        for (k in 0..<length) {
            val index = indices[k]
            if (isHighResolution(index)) {
                continue
            }
            // Fail-safe, should never be hit unless one function is refactored without the other.
            if (this.highResolutionIndicesCount >= MAX_HIGH_RES_COUNT) {
                break
            }
            val avatar = avatarRepository.getOrNull(index) ?: continue
            addedWorldEntities += index
            allWorldEntities += index
            val i = highResolutionIndicesCount++
            highResolutionIndices[i] = index.toShort()
            buffer.p2(avatar.index)
            buffer.p1(avatar.sizeX)
            buffer.p1(avatar.sizeZ)
            buffer.p2(avatar.id)
            val fineXOffset = rootBuildArea.zoneX shl 10
            val fineZOffset = rootBuildArea.zoneZ shl 10
            buffer.encodeAngledCoordFine(
                avatar.currentCoordFine.x - fineXOffset,
                avatar.currentCoordFine.y,
                avatar.currentCoordFine.z - fineZOffset,
                avatar.angle,
            )
            val priority = avatar.priorityTowards(localIndex)
            buffer.p1(priority.id)
            putWorldEntityExtendedInfo(avatar, buffer)
        }
    }

    private fun putWorldEntityExtendedInfo(
        avatar: WorldEntityAvatar,
        buffer: JagByteBuf,
    ) {
        // No extra flags right now as the extended info system is still primitive
        avatar.extendedInfo.pExtendedInfo(
            oldSchoolClientType,
            buffer,
            localIndex,
            0,
        )
    }

    /**
     * Checks if the world entity at [index] is in high resolution indices.
     * @return whether the world entity is within high resolution.
     */
    internal fun isHighResolution(index: Int): Boolean {
        for (i in 0..<highResolutionIndicesCount) {
            if (highResolutionIndices[i].toInt() == index) {
                return true
            }
        }
        return false
    }

    /**
     * Checks if the world entity avatar is close enough to the player to be rendered.
     * This function will always render the world entity which has been marked as active.
     * Any world entities which do not fully fit into the build area will be excluded.
     * This function will check world entities near the player's real coordinate,
     * as well as the coordinate at which the camera is placed.
     */
    private fun inRange(avatar: WorldEntityAvatar): Boolean {
        return isWorldInRange(
            source = this.coordInRootWorld,
            rootWorldBuildArea = this.rootBuildArea,
            worldCenterCoordGrid = avatar.currentCoordGrid,
            radius = renderDistance,
        )
    }

    /**
     * @return If the [coordGrid] is on a world entity, returns that world entity's center coord grid in the root
     * world, otherwise the same input [coordGrid].
     */
    internal fun getCoordGridInRootWorld(coordGrid: CoordGrid): CoordGrid {
        val index = avatarRepository.getByCoordGrid(coordGrid)
        if (index == -1) {
            return coordGrid
        }

        val worldEntity =
            avatarRepository.getOrNull(index)
                ?: return coordGrid
        return worldEntity.currentCoordGrid
    }

    /**
     * Gets the coord fine value in the root world. If the coord grid is inside a world entity,
     * returns the world entity's own coord fine, otherwise converts the coordgrid into a centered coord fine.
     * Note that the y coordinate is set to the level of where the coord is, or is projected into.
     * @param coordGrid the absolute coord grid in the root world
     */
    private fun getCoordFineInRootWorld(coordGrid: CoordGrid): CoordFine {
        val index = avatarRepository.getByCoordGrid(coordGrid)
        if (index == -1) {
            return coordGrid.toCenterCoordFine()
        }

        val worldEntity =
            avatarRepository.getOrNull(index)
                ?: return coordGrid.toCenterCoordFine()
        return worldEntity.currentCoordFine.copy(y = worldEntity.currentCoordGrid.level)
    }

    /**
     * Gets the index of the world entity which contains the provided [coordGrid].
     * @return index of the world entity containing the coord grid, or -1.
     */
    private fun getWorldEntityIndex(coordGrid: CoordGrid): Int {
        return avatarRepository.getByCoordGrid(coordGrid)
    }

    /**
     * Converts the coord grid to a centered coord fine.
     * Note that the y coordinate is set to the level of the coord grid.
     * @return CoordFine representing the center of the coord grid.
     */
    private fun CoordGrid.toCenterCoordFine(): CoordFine {
        return CoordFine(
            (x shl 7) or 0x40,
            level,
            (z shl 7) or 0x40,
        )
    }

    /**
     * Gets the index of the world entity in which the coord grid exists.
     * @return index of the world entity, or -1 if it's in the root world.
     */
    internal fun getWorldEntity(coordGrid: CoordGrid): Int {
        return avatarRepository.getByCoordGrid(coordGrid)
    }

    /**
     * Gets the active level of the world [worldId], or -1 if that world cannot be found.
     */
    internal fun getActiveLevel(worldId: Int): Int {
        val avatar = avatarRepository.getOrNull(worldId)
        return avatar?.activeLevel ?: -1
    }

    /**
     * Checks if the [target] is visible to the [source] based within the scope of the root world.
     * @param source the source player's root coord grid.
     * @param target the target's root coord grid.
     * @param radius the radius in tiles to check when a Chebyshev distance check is performed.
     */
    internal fun isVisibleInRoot(
        source: CoordGrid,
        target: CoordGrid,
        radius: Int,
    ): Boolean {
        // Make sure that the two are within the specified radius distance, and that the target is
        // contained within the source's root world build area.
        // Latter is necessary as we may be on the edge of our currently loaded root map,
        // while the other entity is just outside it - we wouldn't want to include them then.
        return source.inDistance(target, radius) &&
            this.rootBuildArea.contains(target)
    }

    /**
     * Checks if the [target] is visible to the [source] based on the world info we have.
     * If both coordinates are in the root world, a simple Chebyshev distance check is performed.
     * If both coordgrids belong to the same non-root world, the target will be visible.
     * If the target is in a world entity, they will only be visible if the world entity itself is also.
     * Lastly, if the source coordgrid is not in root world, the center coordgrid of the world entity is used,
     * and regular Chebyshev distance check is performed.
     * @param source the source player's root coord grid.
     * @param target the target's root coord grid.
     * @param radius the radius in tiles to check when a Chebyshev distance check is performed (not necessarily always).
     */
    internal fun isVisible(
        source: CoordGrid,
        target: CoordGrid,
        radius: Int,
    ): Boolean {
        val sourceWorldIndex = avatarRepository.getByCoordGrid(source)
        val targetWorldIndex = avatarRepository.getByCoordGrid(target)
        // If both parties are in the root world, just run the usual checks
        if (sourceWorldIndex == ROOT_WORLD && targetWorldIndex == ROOT_WORLD) {
            return source.inDistance(target, radius) &&
                this.rootBuildArea.contains(target)
        }
        // If both parties are on the same worldentity, both should always render if they're on the same level.
        if (sourceWorldIndex == targetWorldIndex) {
            return source.level == target.level
        }
        if (targetWorldIndex != ROOT_WORLD) {
            // If we can see the target worldentity itself,
            // we should also be able to see everything and anyone on it.
            // Similarly, if we cannot see the target worldentity itself, we shouldn't also see
            // anything on it.
            if (!isHighResolution(targetWorldIndex)) {
                return false
            }
            val targetWorld = avatarRepository.getOrNull(targetWorldIndex)
            return targetWorld != null && targetWorld.activeLevel == target.level
        }

        var sourceCoordInRootWorld = source

        // Since the source is guaranteed to be on a world entity by this section,
        // get the world entity's center coord grid.
        val sourceWorld = avatarRepository.getOrNull(sourceWorldIndex)
        if (sourceWorld != null) {
            sourceCoordInRootWorld = sourceWorld.currentCoordGrid
        }

        // Make sure that the two are within the specified radius distance, and that the target is
        // contained within the source's root world build area.
        // Latter is necessary as we may be on the edge of our currently loaded root map,
        // while the other entity is just outside it - we wouldn't want to include them then.
        return sourceCoordInRootWorld.inDistance(target, radius) &&
            this.rootBuildArea.contains(target)
    }

    /**
     * Checks to see if a worldentity avatar is in range of us.
     * @param source the real coordinate of the player, be that on a worldentity or in root world
     * @param rootWorldBuildArea the build area within the root world, to ensure no world entities
     * get picked up that are in the "void"
     * @param worldCenterCoordGrid the pivot point of the target world entity in the root world,
     * not its real coordinate, but rather where it gets projected
     * @param radius the maximum Chebyshev distance to accept
     * @return true if the target world is in range
     */
    private fun isWorldInRange(
        source: CoordGrid,
        rootWorldBuildArea: BuildArea,
        worldCenterCoordGrid: CoordGrid,
        radius: Int,
    ): Boolean {
        val sourceWorldIndex = avatarRepository.getByCoordGrid(source)
        val targetWorldIndex = avatarRepository.getByCoordGrid(worldCenterCoordGrid)
        // If both parties are on the same worldentity, both should always render.
        if (sourceWorldIndex != ROOT_WORLD && sourceWorldIndex == targetWorldIndex) {
            return true
        }
        var sourceCoordInRootWorld = source
        var targetCoordInRootWorld = worldCenterCoordGrid

        // If the source is on a worldentity, use the world entity's coord in root world.
        if (sourceWorldIndex != ROOT_WORLD) {
            val sourceWorld = avatarRepository.getOrNull(sourceWorldIndex)
            if (sourceWorld != null) {
                sourceCoordInRootWorld = sourceWorld.currentCoordGrid
            }
        }

        // If the target is on a worldentity, use the world entity's coord in root world.
        if (targetWorldIndex != ROOT_WORLD) {
            val targetWorld = avatarRepository.getOrNull(targetWorldIndex)
            if (targetWorld != null) {
                targetCoordInRootWorld = targetWorld.currentCoordGrid
            }
        }

        // Make sure that the two are within the specified radius distance, and that the target is
        // contained within the source's root world build area.
        // Latter is necessary as we may be on the edge of our currently loaded root map,
        // while the other world entity is just outside it - we wouldn't want to include them then.
        return sourceCoordInRootWorld.inDistance(targetCoordInRootWorld, radius) &&
            rootWorldBuildArea.contains(targetCoordInRootWorld)
    }

    private fun isReallocated(avatar: WorldEntityAvatar): Boolean {
        // If the avatar was allocated on this cycle, ensure we remove (and potentially re-add later)
        // this avatar. This is due to a worldentity of the same index being deallocated and reallocated
        // as a new instance in the same cycle.
        return avatar.allocateCycle == WorldEntityProtocol.cycleCount
    }

    override fun onAlloc(
        index: Int,
        oldSchoolClientType: OldSchoolClientType,
        newInstance: Boolean,
    ) {
        checkCommunicationThread()
        this.localIndex = index
        this.oldSchoolClientType = oldSchoolClientType
        this.renderDistance = DEFAULT_RENDER_DISTANCE
        this.coordInRootWorld = CoordGrid.INVALID
        this.rootBuildArea = BuildArea.INVALID
        this.highResolutionIndicesCount = 0
        this.highResolutionIndices.fill(0)
        this.temporaryHighResolutionIndices.fill(0)
        this.allWorldEntities.clear()
        this.addedWorldEntities.clear()
        this.removedWorldEntities.clear()
        this.buffer = null
        this.exception = null
        this.previousPacket = null
    }

    /**
     * Resets any existing world entity state, as a clean state is required.
     */
    public fun onReconnect() {
        checkCommunicationThread()
        if (isDestroyed()) return
        this.buffer = null
        this.exception = null
        this.previousPacket = null
        this.highResolutionIndicesCount = 0
        this.highResolutionIndices.fill(0)
        this.temporaryHighResolutionIndices.fill(0)
        this.allWorldEntities.clear()
        this.addedWorldEntities.clear()
        this.removedWorldEntities.clear()
    }

    override fun onDealloc() {
        checkCommunicationThread()
        this.buffer = null
        this.previousPacket = null
    }

    public companion object {
        /**
         * The index value that marks a termination for high resolution indices.
         */
        private const val INDEX_TERMINATOR: Short = -1

        /**
         * The terminator value that indicates that there are no more world entities in the
         * corresponding zone.
         */
        private const val WORLDENTITY_LOOKUP_TERMINATOR: Int = 0xFFFF

        /**
         * The maximum number of high resolution world entities that could be sent.
         */
        private const val MAX_HIGH_RES_COUNT: Int = 25

        /**
         * The id of the root world.
         */
        public const val ROOT_WORLD: Int = -1

        /**
         * The default render distance for world entities.
         */
        private const val DEFAULT_RENDER_DISTANCE: Int = 31

        /**
         * The default radius to seek zones from around the center point.
         */
        private const val DEFAULT_ZONE_SEEK_RADIUS: Int = 3

        /**
         * The default capacity of the backing byte buffer into which all world info is written.
         * The size here is calculated by taking the high res count byte + (max removals) + (max additions),
         * creating the maximum theoretically possible buffer as a result of it.
         * If the packet ever changes, this MUST be adjusted accordingly.
         */
        private const val BUF_CAPACITY: Int = 1 + (MAX_HIGH_RES_COUNT * 1) + (MAX_HIGH_RES_COUNT * 10)

        private val logger = InlineLogger()
    }
}
