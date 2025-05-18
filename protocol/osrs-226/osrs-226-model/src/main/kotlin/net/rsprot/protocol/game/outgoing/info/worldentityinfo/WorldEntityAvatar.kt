package net.rsprot.protocol.game.outgoing.info.worldentityinfo

import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufAllocator
import net.rsprot.buffer.extensions.p1
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprot.protocol.game.outgoing.info.util.Avatar
import net.rsprot.protocol.internal.checkCommunicationThread
import net.rsprot.protocol.internal.game.outgoing.info.CoordFine
import net.rsprot.protocol.internal.game.outgoing.info.CoordGrid
import net.rsprot.protocol.internal.game.outgoing.info.util.ZoneIndexStorage

/**
 * A world entity avatar represents a dynamic world entity as a single unit.

 * @property allocator the byte buffer allocator to be used for the high resolution
 * movement buffer of this world entity.
 * @property zoneIndexStorage the storage responsible for tracking world entities across
 * zones.
 * @property index the index of this world entity.
 * @property sizeX the width of the world entity in zones.
 * @property sizeZ the height of the world entity in zones.
 * @property currentCoordFine the coordinate that this world entity is being rendered at.
 * @property angle the current angle of this world entity.
 * @property lastCoordFine the last known coordinate of the world entity by the client.
 * @property highResolutionBuffer the buffer which contains the pre-computed high resolution
 * movement of this avatar.
 */
public class WorldEntityAvatar(
    internal val allocator: ByteBufAllocator,
    internal val zoneIndexStorage: ZoneIndexStorage,
    internal var index: Int,
    internal var sizeX: Int,
    internal var sizeZ: Int,
    internal var level: Int,
    internal var currentCoordFine: CoordFine = CoordFine.INVALID,
    internal var angle: Int,
) : Avatar {
    internal var teleport: Boolean = false
    internal var lastAngle: Int = angle
    internal var lastCoordFine: CoordFine = currentCoordFine

    internal var highResolutionBuffer: ByteBuf? = null

    internal val currentCoordGrid: CoordGrid
        get() = currentCoordFine.toCoordGrid(this.level)

    /**
     * The [WorldEntityProtocol.cycleCount] when this avatar was allocated.
     * We use this to determine whether to perform a re-synchronization of a worldentity,
     * which can happen when a worldentity is deallocated and reallocated on the same cycle,
     * which could result in other clients not seeing any change take place. While rare,
     * this possibility exists, and it could result in some rather odd bugs.
     */
    internal var allocateCycle: Int = WorldEntityProtocol.cycleCount

    /**
     * Precomputes the high resolution buffer of this world entity.
     */
    internal fun precompute() {
        if (this.currentCoordFine == this.lastCoordFine && this.angle == this.lastAngle) {
            val buffer = allocator.buffer(1, 1)
            this.highResolutionBuffer = buffer
            // Opcode 1 indicates no change
            buffer.p1(1)
            return
        }
        val buffer =
            allocator
                .buffer(MAX_HIGH_RES_BUF_SIZE, MAX_HIGH_RES_BUF_SIZE)
                .toJagByteBuf()
        this.highResolutionBuffer = buffer.buffer

        // Opcode 3 indicates teleport, 2 indicates smooth movement
        if (this.teleport) {
            buffer.p1(3)
        } else {
            buffer.p1(2)
        }
        val dx = currentCoordFine.x - lastCoordFine.x
        val dy = currentCoordFine.y - lastCoordFine.y
        val dz = currentCoordFine.z - lastCoordFine.z
        val dAngle = angle - lastAngle
        buffer.encodeAngledCoordFine(dx, dy, dz, dAngle)
    }

    /**
     * Updates the current coordinate of this world entity, along with a move speed
     * to reach that coordinate, if applicable.
     * @param level the current absolute level of this world entity.
     * @param fineX the absolute fine x coordinate of this world entity. This coordinate is effectively
     * absolute coordinate * 128.
     * @param fineY the fine y coordinate (or the height) of this world entity. This value
     * should be in range of 0..1023. Note that as of revision 226, this property is overwritten by the
     * ground height and has no impact on the perceived height of the world entity.
     * @param fineZ the absolute fine z coordinate of this world entity. This coordinate is effectively
     * absolute coordinate * 128.
     * @param teleport whether to jump the worldentity to the desired coordinate.
     */
    @Throws(IllegalArgumentException::class)
    public fun updateCoord(
        level: Int,
        fineX: Int,
        fineY: Int,
        fineZ: Int,
        teleport: Boolean,
    ) {
        checkCommunicationThread()
        val coordFine = CoordFine(fineX, fineY, fineZ)
        val coordGrid = coordFine.toCoordGrid(level)
        this.zoneIndexStorage.remove(this.index, currentCoordGrid)
        this.currentCoordFine = coordFine
        this.level = level
        this.zoneIndexStorage.add(this.index, coordGrid)
        this.teleport = teleport
    }

    /**
     * Updates the current angle of this world entity.
     * It should be noted that the client is only made to rotate by a maximum of 22.5 degrees (128/2048 units)
     * per game cycle, so it may take multiple seconds for it to finish the turn.
     */
    public fun updateAngle(angle: Int) {
        checkCommunicationThread()
        this.angle = angle
    }

    override fun postUpdate() {
        this.lastCoordFine = this.currentCoordFine
        this.highResolutionBuffer?.release()
        this.teleport = false
        this.lastAngle = this.angle
    }

    private companion object {
        /**
         * The maximum buffer size for the high resolution precomputed buffer.
         */
        private const val MAX_HIGH_RES_BUF_SIZE: Int = 18
    }
}
