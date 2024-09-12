package net.rsprot.protocol.game.outgoing.info.worldentityinfo

import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufAllocator
import net.rsprot.buffer.extensions.p1
import net.rsprot.buffer.extensions.p2
import net.rsprot.protocol.common.game.outgoing.info.CoordGrid
import net.rsprot.protocol.game.outgoing.info.util.Avatar

/**
 * A world entity avatar represents a dynamic world entity as a single unit.
 *
 * Movement speed table:
 * ```kt
 * | Id | Speed (Tiles/Cycle) |
 * |:--:|:-------------------:|
 * | -1 |    Instantaneous    |
 * |  0 |         0.5         |
 * |  1 |         1.0         |
 * |  2 |         1.5         |
 * |  3 |         2.0         |
 * |  4 |         2.5         |
 * |  5 |         3.0         |
 * |  6 |         3.5         |
 * |  7 |         4.0         |
 * ```
 *
 * @property allocator the byte buffer allocator to be used for the high resolution
 * movement buffer of this world entity.
 * @property index the index of this world entity.
 * @property sizeX the width of the world entity in zones.
 * @property sizeZ the height of the world entity in zones.
 * @property currentCoord the coordinate that this world entity is being rendered at.
 * @property angle the current angle of this world entity.
 * @property moveSpeed the current movement speed of this world entity. See the table above
 * for a full description of every possible move speed.
 * @property lastCoord the last known coordinate of the world entity by the client.
 * @property highResolutionBuffer the buffer which contains the pre-computed high resolution
 * movement of this avatar.
 */
public class WorldEntityAvatar(
    internal val allocator: ByteBufAllocator,
    internal var index: Int,
    internal var sizeX: Int,
    internal var sizeZ: Int,
    internal var currentCoord: CoordGrid = CoordGrid.INVALID,
    internal var angle: Int,
) : Avatar {
    private var moveSpeed: Int = -1
    internal var lastCoord: CoordGrid = currentCoord

    internal var highResolutionBuffer: ByteBuf? = null

    /**
     * Precomputes the high resolution buffer of this world entity.
     */
    internal fun precompute() {
        val buffer = allocator.buffer(MAX_HIGH_RES_BUF_SIZE, MAX_HIGH_RES_BUF_SIZE)
        this.highResolutionBuffer = buffer
        val dx = currentCoord.x - lastCoord.x
        val dz = currentCoord.z - lastCoord.z
        buffer.p1(currentCoord.level)
        buffer.p1(dx)
        buffer.p1(dz)
        buffer.p2(angle)
        buffer.p1(moveSpeed)
    }

    /**
     * Updates the current coordinate of this world entity, along with a move speed
     * to reach that coordinate, if applicable.
     * @param level the current level of this world entity.
     * @param x the current absolute x coordinate of this world entity.
     * @param z the current absolute z coordinate of this world entity.
     * @param moveSpeed the movement speed of this world entity. See the table within
     * the main class documentation for the possible move speed values.
     */
    @Throws(IllegalArgumentException::class)
    public fun updateCoord(
        level: Int,
        x: Int,
        z: Int,
        moveSpeed: Int,
    ) {
        this.currentCoord = CoordGrid(level, x, z)
        this.moveSpeed = moveSpeed
    }

    /**
     * Updates the current angle of this world entity.
     * It should be noted that the client is only made to rotate by a maximum of 22.5 degrees (128/2048 units)
     * per game cycle, so it may take multiple seconds for it to finish the turn.
     */
    public fun updateAngle(angle: Int) {
        this.angle = angle
    }

    override fun postUpdate() {
        this.lastCoord = this.currentCoord
        this.highResolutionBuffer?.release()
    }

    private companion object {
        /**
         * The maximum buffer size for the high resolution precomputed buffer.
         */
        private const val MAX_HIGH_RES_BUF_SIZE: Int = 5
    }
}
