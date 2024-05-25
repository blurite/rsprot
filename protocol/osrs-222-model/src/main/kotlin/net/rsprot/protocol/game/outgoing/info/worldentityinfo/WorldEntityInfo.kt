package net.rsprot.protocol.game.outgoing.info.worldentityinfo

import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufAllocator
import net.rsprot.buffer.JagByteBuf
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprot.protocol.common.client.OldSchoolClientType
import net.rsprot.protocol.common.game.outgoing.info.CoordGrid
import net.rsprot.protocol.game.outgoing.info.util.ReferencePooledObject

public class WorldEntityInfo internal constructor(
    internal var localIndex: Int,
    internal val allocator: ByteBufAllocator,
    private var oldSchoolClientType: OldSchoolClientType,
    private val avatarRepository: WorldEntityAvatarRepository,
    private val indexSupplier: WorldEntityIndexSupplier,
) : ReferencePooledObject {
    private var renderDistance: Int = 15
    private var currentLocalPlayerCoord: CoordGrid = CoordGrid.INVALID
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
    private var buffer: ByteBuf? = null
    internal var exception: Exception? = null
    private var builtIntoPacket: Boolean = false

    public fun updateRenderDistance(distance: Int) {
        this.renderDistance = distance
    }

    public fun updateBuildArea(buildArea: BuildArea) {
        this.buildArea = buildArea
    }

    public fun updateCoord(
        level: Int,
        x: Int,
        z: Int,
    ) {
        this.currentLocalPlayerCoord = CoordGrid(level, x, z)
    }

    @Throws(IllegalStateException::class)
    public fun backingBuffer(): ByteBuf {
        return checkNotNull(buffer)
    }

    private fun allocBuffer(): ByteBuf {
        // Acquire a new buffer with each cycle, in case the previous one isn't fully written out yet
        val buffer = allocator.buffer(BUF_CAPACITY, BUF_CAPACITY)
        this.buffer = buffer
        this.builtIntoPacket = false
        return buffer
    }

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

    internal fun updateWorldEntities() {
        val buffer = allocBuffer().toJagByteBuf()
        val fragmented = processHighResolution(buffer)
        if (fragmented) {
            defragmentIndices()
        }
        processLowResolution(buffer)
    }

    private fun processHighResolution(buffer: JagByteBuf): Boolean {
        var fragmented = false
        buffer.p1(this.highResolutionIndicesCount)
        for (i in 0..<this.highResolutionIndicesCount) {
            val index = this.highResolutionIndices[i].toInt()
            val avatar = avatarRepository.getOrNull(index)
            if (avatar == null || !inRange(avatar)) {
                fragmented = true
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
        return fragmented
    }

    private fun processLowResolution(buffer: JagByteBuf) {
        if (this.highResolutionIndicesCount >= MAX_HIGH_RES_COUNT) {
            return
        }
        val (level, x, z) = this.currentLocalPlayerCoord
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

    private fun isHighResolution(index: Int): Boolean {
        for (i in 0..<highResolutionIndicesCount) {
            if (highResolutionIndices[i].toInt() == index) {
                return true
            }
        }
        return false
    }

    private fun inRange(avatar: WorldEntityAvatar): Boolean {
        if (avatar !in buildArea) {
            return false
        }
        // Potentially make it be based on center coord?
        // Not sure how nice it looks with just the south-west tile checks
        return avatar.currentCoord.inDistance(this.currentLocalPlayerCoord, renderDistance)
    }

    internal fun afterUpdate() {
        // TODO
    }

    override fun onAlloc(
        index: Int,
        oldSchoolClientType: OldSchoolClientType,
    ) {
        this.localIndex = index
        this.oldSchoolClientType = oldSchoolClientType
    }

    override fun onDealloc() {
        // TODO
    }

    private companion object {
        private const val INDEX_TERMINATOR: Short = -1
        private const val MAX_HIGH_RES_COUNT: Int = 255

        /**
         * The default capacity of the backing byte buffer into which all world info is written.
         * The size here is calculated by taking the high res count byte + (max removals) + (max additions),
         * creating the maximum theoretically possible buffer as a result of it.
         * If the packet ever changes, this MUST be adjusted accordingly.
         */
        private const val BUF_CAPACITY: Int = 1 + (MAX_HIGH_RES_COUNT * 1) + (MAX_HIGH_RES_COUNT * 10)
    }
}
