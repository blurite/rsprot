package net.rsprot.protocol.game.outgoing.info.worldentityinfo

import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufAllocator
import net.rsprot.buffer.JagByteBuf
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprot.protocol.common.client.OldSchoolClientType
import net.rsprot.protocol.common.game.outgoing.info.CoordGrid
import net.rsprot.protocol.game.outgoing.info.exceptions.InfoProcessException
import net.rsprot.protocol.game.outgoing.info.util.BuildArea
import net.rsprot.protocol.game.outgoing.info.util.ReferencePooledObject

/**
 * The world entity info class tracks everything about the world entities that
 * are near this player.
 * @property localIndex the index of the local player that owns this world entity info.
 * @property allocator the byte buffer allocator used to build the buffer for the packet.
 * @property oldSchoolClientType the client type on which the player has logged in.
 * @property avatarRepository the avatar repository keeping track of every known
 * world entity in the root world.
 * @property indexSupplier the implementation returning all the indices of the world
 * entities near the local player.
 * @property renderDistance the render distance in tiles, effectively how far to render
 * world entities from the local player (or the camera pov)
 * @property currentWorldEntityId the id of the world entity on which the local player
 * currently resides.
 * @property currentCoord the current real coordinate of the local player.
 * @property buildArea the current build area of the player, this is the root base
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
 * @property builtIntoPacket whether the buffer has been built into a packet,
 * which means the responsibility of releasing the buffer falls to the server.
 * If false, the protocol will release the buffer when this instance is being
 * deallocated.
 * @property exception the exception that was caught during the computations
 * of this world entity info, if any. This will be thrown as the [toPacket]
 * function is called, allowing the server to handle it from the correct
 * perspective of the caller, as the protocol itself is computed in for the
 * entire server in one go.
 * @property renderCoord if the player is currently on a world entity, this marks the coordinate
 * at which the world entity is being rendered on the root world. This allows the protocol
 * to still see other world entities nearby despite the player being in an instance.
 */
@Suppress("MemberVisibilityCanBePrivate")
public class WorldEntityInfo internal constructor(
    internal var localIndex: Int,
    internal val allocator: ByteBufAllocator,
    private var oldSchoolClientType: OldSchoolClientType,
    private val avatarRepository: WorldEntityAvatarRepository,
    private val indexSupplier: WorldEntityIndexSupplier,
) : ReferencePooledObject {
    private var renderDistance: Int = DEFAULT_RENDER_DISTANCE
    private var currentWorldEntityId: Int = ROOT_WORLD
    private var currentCoord: CoordGrid = CoordGrid.INVALID
    private var buildArea: BuildArea = BuildArea.INVALID
    private var highResolutionIndicesCount: Int = 0
    private var highResolutionIndices: ShortArray =
        ShortArray(WorldEntityProtocol.CAPACITY) {
            INDEX_TERMINATOR
        }
    private var temporaryHighResolutionIndices: ShortArray =
        ShortArray(WorldEntityProtocol.CAPACITY) {
            INDEX_TERMINATOR
        }
    private val allWorldEntities = ArrayList<Int>()
    private val addedWorldEntities = ArrayList<Int>()
    private val removedWorldEntities = ArrayList<Int>()
    private var buffer: ByteBuf? = null
    internal var exception: Exception? = null
    private var builtIntoPacket: Boolean = false
    private var renderCoord: CoordGrid = CoordGrid.INVALID

    /**
     * Updates the render distance for this player, potentially allowing
     * the world entities to be rendered from further away. All instances
     * will however still be constrained to within the build area, in their
     * entirety - if they cannot fulfill that constraint, they will not be
     * put into high resolution view.
     * @param distance the distance in tiles how far the world entities should
     * be rendered from the player (or the camera's POV)
     */
    public fun updateRenderDistance(distance: Int) {
        this.renderDistance = distance
    }

    /**
     * Updates the build area for this player. This should always perfectly correspond to
     * the actual build area that is sent via REBUILD_NORMAL or REBUILD_REGION packets.
     * @param buildArea the build area in which everything is rendered.
     */
    public fun updateBuildArea(buildArea: BuildArea) {
        this.buildArea = buildArea
    }

    /**
     * Gets a list of all the world entity indices that are currently in high resolution,
     * allowing for correct functionality for player and npc infos, as well as zone updates.
     * @return a list of indices of the world entities currently in high resolution.
     */
    public fun getAllWorldEntityIndices(): List<Int> = this.allWorldEntities

    /**
     * Gets the indices of all the world entities that were added to high resolution in this cycle,
     * allowing the server to allocate new player and npc info instances, and sync the state of the
     * zones in those world entities.
     * @return a list of all the world entity indices added to the high resolution view in this
     * cycle.
     */
    public fun getAddedWorldEntityIndices(): List<Int> = this.addedWorldEntities

    /**
     * Gets the indices of all the world entities that were removed from the high resolution in
     * this cycle, allowing the server to destroy the player and npc info instances corresponding
     * to them, and to clear the zones that were being tracked due to it.
     * @return a list of all the indices of the world entities that were removed from the high
     * resolution view this cycle.
     */
    public fun getRemovedWorldEntityIndices(): List<Int> = this.removedWorldEntities

    /**
     * Updates the current real absolute coordinate of the local player in the world.
     * @param worldId the id of the world in which the player currently resides,
     * if they are inside a world entity, this would be that index. If they are in the
     * root world, this should be [ROOT_WORLD].
     * @param level the current height level of the player
     * @param x the current absolute x coordinate of the player
     * @param z the current absolute z coordinate of the player
     */
    public fun updateCoord(
        worldId: Int,
        level: Int,
        x: Int,
        z: Int,
    ) {
        this.currentWorldEntityId = worldId
        this.currentCoord = CoordGrid(level, x, z)
    }

    /**
     * Sets the render coordinate of this player. This function should only be used
     * when the player is inside one of the world entities. The value should correspond
     * to the coordinate at which the world entity in which the player resides, in the
     * root world - not in the instance land.
     * @param level the level of the render coordinate
     * @param x the absolute x value of the render coordinate
     * @param z the absolute z value of the render coordinate
     */
    public fun setRenderCoord(
        level: Int,
        x: Int,
        z: Int,
    ) {
        this.renderCoord = CoordGrid(level, x, z)
    }

    /**
     * Resets the render coordinate. This function should be called when the player
     * leaves one of the dynamic world entities and moves back onto the root world.
     */
    public fun resetRenderCoord() {
        this.renderCoord = CoordGrid.INVALID
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
     * which can be flushed to the client.
     * If an exception was caught during the computation of this world entity info,
     * it will be thrown in here, allowing the server to properly handle exceptions
     * in a per-player perspective.
     * @return the world entity packet instance.
     */
    public fun toPacket(): WorldEntityInfoPacket {
        val exception = this.exception
        if (exception != null) {
            throw InfoProcessException(
                "Exception occurred during player info processing for index $localIndex",
                exception,
            )
        }
        builtIntoPacket = true
        return WorldEntityInfoPacket(backingBuffer())
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
        this.builtIntoPacket = false
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
        val fragmented = processHighResolution(buffer)
        if (fragmented) {
            defragmentIndices()
        }
        processLowResolution(buffer)
    }

    /**
     * Processes all the currently tracked high resolution world entities.
     * @param buffer the buffer into which to write the high resolution updates.
     * @return whether any world entities were removed from high resolution, meaning
     * a defragmentation process is necessary.
     */
    private fun processHighResolution(buffer: JagByteBuf): Boolean {
        val count = this.highResolutionIndicesCount
        buffer.p1(count)
        for (i in 0..<count) {
            val index = this.highResolutionIndices[i].toInt()
            val avatar = avatarRepository.getOrNull(index)
            if (avatar == null || !inRange(avatar)) {
                highResolutionIndices[i] = INDEX_TERMINATOR
                this.highResolutionIndicesCount--
                this.removedWorldEntities += index
                allWorldEntities -= index
                buffer.p1(0)
                continue
            }
            buffer.p1(1)
            val precomputedBuffer = checkNotNull(avatar.highResolutionBuffer)
            buffer.buffer.writeBytes(
                precomputedBuffer,
                precomputedBuffer.readerIndex(),
                precomputedBuffer.readableBytes(),
            )
        }
        return count != this.highResolutionIndicesCount
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
        val currentWorld = this.currentWorldEntityId
        val (level, x, z) =
            if (currentWorld == ROOT_WORLD) {
                this.currentCoord
            } else {
                val worldEntity = checkNotNull(avatarRepository.getOrNull(currentWorld))
                // Perhaps center coord instead?
                worldEntity.currentCoord
            }
        val entities =
            indexSupplier.supply(
                this.localIndex,
                level,
                x,
                z,
                this.renderDistance,
            )
        while (entities.hasNext()) {
            val index = entities.next() and 0xFFFF
            if (index == 0xFFFF || isHighResolution(index)) {
                continue
            }
            if (this.highResolutionIndicesCount >= MAX_HIGH_RES_COUNT) {
                break
            }
            val avatar = avatarRepository.getOrNull(index) ?: continue
            // Secondary build-area distance check
            if (!inRange(avatar)) {
                continue
            }
            addedWorldEntities += index
            allWorldEntities += index
            val i = highResolutionIndicesCount++
            highResolutionIndices[i] = index.toShort()
            buffer.p2(avatar.index)
            buffer.p1(avatar.sizeX)
            buffer.p1(avatar.sizeZ)
            val buildAreaCoord = buildArea.localize(avatar.currentCoord)
            buffer.p1(buildAreaCoord.xInBuildArea)
            buffer.p1(buildAreaCoord.zInBuildArea)
            buffer.p2(avatar.angle)
            // The zero is a currently unassigned property on all clients
            buffer.p2(0)
        }
    }

    /**
     * Checks if the world entity at [index] is in high resolution indices.
     * @return whether the world entity is within high resolution.
     */
    private fun isHighResolution(index: Int): Boolean {
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
        // Always render the world entity the player is on
        if (avatar.index == currentWorldEntityId) {
            return true
        }
        if (avatar !in buildArea) {
            return false
        }
        // Potentially make it be based on center coord?
        // Not sure how nice it looks with just the south-west tile checks
        return avatar.currentCoord.inDistance(
            this.currentCoord,
            renderDistance,
        ) ||
            (
                renderCoord != CoordGrid.INVALID &&
                    avatar.currentCoord.inDistance(
                        this.renderCoord,
                        renderDistance,
                    )
            )
    }

    override fun onAlloc(
        index: Int,
        oldSchoolClientType: OldSchoolClientType,
    ) {
        this.localIndex = index
        this.oldSchoolClientType = oldSchoolClientType
        this.renderDistance = DEFAULT_RENDER_DISTANCE
        this.currentWorldEntityId = ROOT_WORLD
        this.currentCoord = CoordGrid.INVALID
        this.buildArea = BuildArea.INVALID
        this.renderCoord = CoordGrid.INVALID
        this.highResolutionIndicesCount = 0
        this.highResolutionIndices.fill(0)
        this.temporaryHighResolutionIndices.fill(0)
        this.allWorldEntities.clear()
        this.addedWorldEntities.clear()
        this.removedWorldEntities.clear()
        this.builtIntoPacket = false
        this.buffer = null
        this.exception = null
    }

    /**
     * Resets any existing world entity state, as a clean state is required.
     */
    public fun onReconnect() {
        if (!builtIntoPacket) {
            val buffer = this.buffer
            if (buffer != null && buffer.refCnt() > 0) {
                buffer.release(buffer.refCnt())
            }
            this.builtIntoPacket = false
            this.buffer = null
            this.exception = null
        }
        this.highResolutionIndicesCount = 0
        this.highResolutionIndices.fill(0)
        this.temporaryHighResolutionIndices.fill(0)
        this.allWorldEntities.clear()
        this.addedWorldEntities.clear()
        this.removedWorldEntities.clear()
    }

    override fun onDealloc() {
        if (!builtIntoPacket) {
            val buffer = this.buffer
            if (buffer != null && buffer.refCnt() > 0) {
                buffer.release(buffer.refCnt())
            }
        }
    }

    private companion object {
        /**
         * The index value that marks a termination for high resolution indices.
         */
        private const val INDEX_TERMINATOR: Short = -1

        /**
         * The maximum number of high resolution world entities that could be sent.
         */
        private const val MAX_HIGH_RES_COUNT: Int = 255

        /**
         * The id of the root world.
         */
        private const val ROOT_WORLD: Int = -1

        /**
         * The default render distance for world entities.
         */
        private const val DEFAULT_RENDER_DISTANCE: Int = 15

        /**
         * The default capacity of the backing byte buffer into which all world info is written.
         * The size here is calculated by taking the high res count byte + (max removals) + (max additions),
         * creating the maximum theoretically possible buffer as a result of it.
         * If the packet ever changes, this MUST be adjusted accordingly.
         */
        private const val BUF_CAPACITY: Int = 1 + (MAX_HIGH_RES_COUNT * 1) + (MAX_HIGH_RES_COUNT * 10)
    }
}
