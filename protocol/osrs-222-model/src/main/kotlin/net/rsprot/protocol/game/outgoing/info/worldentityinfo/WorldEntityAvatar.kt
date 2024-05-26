package net.rsprot.protocol.game.outgoing.info.worldentityinfo

import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufAllocator
import net.rsprot.buffer.extensions.p1
import net.rsprot.buffer.extensions.p2
import net.rsprot.protocol.common.game.outgoing.info.CoordGrid
import net.rsprot.protocol.game.outgoing.info.util.Avatar

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

    internal fun precompute() {
        val buffer = allocator.buffer(MAX_HIGH_RES_BUF_SIZE, MAX_HIGH_RES_BUF_SIZE)
        this.highResolutionBuffer = buffer
        val dx = currentCoord.x - lastCoord.x
        val dz = currentCoord.z - lastCoord.z
        buffer.p1(dx)
        buffer.p1(dz)
        buffer.p2(angle)
        buffer.p1(moveSpeed)
    }

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

    public fun updateAngle(angle: Int) {
        this.angle = angle
    }

    override fun postUpdate() {
        this.lastCoord = this.currentCoord
        this.highResolutionBuffer?.release()
    }

    private companion object {
        private const val MAX_HIGH_RES_BUF_SIZE: Int = 5
    }
}
