package net.rsprot.protocol.game.outgoing.info.playerinfo

import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufAllocator
import net.rsprot.buffer.bitbuffer.BitBuf
import net.rsprot.buffer.bitbuffer.UnsafeLongBackedBitBuf
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprot.compression.HuffmanCodec
import net.rsprot.protocol.game.outgoing.info.playerinfo.util.CellOpcodes
import net.rsprot.protocol.game.outgoing.info.playerinfo.util.ObserverExtendedInfoFlags
import net.rsprot.protocol.game.outgoing.info.util.Avatar
import net.rsprot.protocol.game.outgoing.info.util.ReferencePooledObject
import net.rsprot.protocol.internal.game.outgoing.info.encoder.ExtendedInfoEncoders
import net.rsprot.protocol.message.OutgoingMessage
import net.rsprot.protocol.shared.platform.PlatformType
import java.util.BitSet
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.math.abs

@Suppress("DuplicatedCode", "ReplaceUntilWithRangeUntil")
public class PlayerInfo internal constructor(
    private val protocol: PlayerInfoProtocol,
    private val localIndex: Int,
    private val capacity: Int,
    private val allocator: ByteBufAllocator,
    private val platformType: PlatformType,
    extendedInfoEncoders: Map<PlatformType, ExtendedInfoEncoders>,
    huffmanCodec: HuffmanCodec,
) : ReferencePooledObject, OutgoingMessage {
    /**
     * The [avatar] represents properties of our local player.
     */
    public val avatar: PlayerAvatar = PlayerAvatar()

    /**
     * Low resolution indices are tracked together with [lowResolutionCount].
     * Whenever a player enters the low resolution view, their index
     * is added into this [lowResolutionIndices] array, and the [lowResolutionCount]
     * is incremented by one.
     * At the end of each cycle, the [lowResolutionIndices] are rebuilt to sort the indices.
     */
    private val lowResolutionIndices: ShortArray = ShortArray(capacity)

    /**
     * The number of players in low resolution according to the protocol.
     */
    private var lowResolutionCount: Int = 0

    /**
     * The tracked high resolution players by their indices.
     * If a player enters our high resolution, the bit at their index is set to true.
     * We do not need to use references to players as we can then refer to the [PlayerInfoRepository]
     * to find the actual [PlayerInfo] implementation.
     */
    private val highResolutionPlayers: BitSet = BitSet(capacity)

    /**
     * High resolution indices are tracked together with [highResolutionCount].
     * Whenever an external player enters the high resolution view, their index
     * is added into this [highResolutionIndices] array, and the [highResolutionCount]
     * is incremented by one.
     * At the end of each cycle, the [highResolutionIndices] are rebuilt to sort the indices.
     */
    private val highResolutionIndices: ShortArray = ShortArray(capacity)

    /**
     * The number of players in high resolution according to the protocol.
     */
    private var highResolutionCount: Int = 0

    private val extendedInfoIndices: ShortArray = ShortArray(capacity)
    private var extendedInfoCount: Int = 0

    private val stationary = ByteArray(capacity)

    /**
     * Extended info repository, commonly referred to as "masks", will track everything relevant
     * inside itself. Setting properties such as a spotanim would be done through this.
     * The [extendedInfo] is also responsible for caching the non-temporary blocks,
     * such as appearance and move speed.
     */
    public val extendedInfo: PlayerAvatarExtendedInfo =
        PlayerAvatarExtendedInfo(
            capacity,
            protocol,
            localIndex,
            extendedInfoEncoders,
            allocator,
            huffmanCodec,
        )

    internal val observerExtendedInfoFlags: ObserverExtendedInfoFlags = ObserverExtendedInfoFlags(capacity)

    private val highResMovementBuffer: UnsafeLongBackedBitBuf = UnsafeLongBackedBitBuf()
    private val lowResMovementBuffer: UnsafeLongBackedBitBuf = UnsafeLongBackedBitBuf()

    private var buffer: ByteBuf? = null

    public fun backingBuffer(): ByteBuf {
        return checkNotNull(buffer)
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
        this.avatar.updateCoord(level, x, z)
    }

    public fun handleAbsolutePlayerPositions(byteBuf: ByteBuf) {
        BitBuf(byteBuf).use { buffer ->
            buffer.pBits(30, avatar.currentCoord.packed)
            highResolutionPlayers.set(localIndex)
            highResolutionIndices[highResolutionCount++] = localIndex.toShort()
            for (i in 1 until capacity) {
                if (i == localIndex) {
                    continue
                }
                val lowResolutionPosition = protocol.getLowResolutionPosition(i)
                buffer.pBits(18, lowResolutionPosition.packed)
                lowResolutionIndices[lowResolutionCount++] = i.toShort()
            }
        }
    }

    /**
     * Precalculates all the bitcodes for this player, for both low-resolution and high-resolution updates.
     * This function will be thread-safe relative to other players and can be calculated concurrently for all players.
     */
    internal fun prepareBitcodes(globalLowResolutionPositionRepository: GlobalLowResolutionPositionRepository) {
        prepareHighResMovement()
        prepareLowResMovement(globalLowResolutionPositionRepository)
    }

    internal fun precomputeExtendedInfo() {
        extendedInfo.precompute()
    }

    internal fun putExtendedInfo() {
        val buffer = backingBuffer().toJagByteBuf()
        for (i in 0 until extendedInfoCount) {
            val index = extendedInfoIndices[i].toInt()
            val other = checkNotNull(protocol.getPlayerInfo(index))
            val observerFlag = observerExtendedInfoFlags.getFlag(index)
            other.extendedInfo.pExtendedInfo(platformType, buffer, observerFlag, this.localIndex)
        }
    }

    /**
     * Writes to the actual buffers the prepared bitcodes and extended information.
     * This function will be thread-safe relative to other players and can be calculated concurrently for all players.
     */
    internal fun pBitcodes() {
        avatar.resize(highResolutionCount)
        val buffer = allocBuffer()
        val bitbuf = BitBuf(buffer)
        bitbuf.use { processHighResolution(it, skipUnmodified = true) }
        bitbuf.use { processHighResolution(it, skipUnmodified = false) }
        bitbuf.use { processLowResolution(it, skipUnmodified = false) }
        bitbuf.use { processLowResolution(it, skipUnmodified = true) }
    }

    private fun processLowResolution(
        buffer: BitBuf,
        skipUnmodified: Boolean,
    ) {
        var skips = 0
        for (i in 0 until lowResolutionCount) {
            val index = lowResolutionIndices[i].toInt()
            val isUnmodified = stationary[index].toInt() and 0x1 != 0
            if (skipUnmodified == isUnmodified) {
                continue
            }
            val other = protocol.getPlayerInfo(index)
            if (other == null) {
                skips++
                stationary[index] = (stationary[index].toInt() or 0x2).toByte()
            } else if (isVisible(other)) {
                if (skips > 0) {
                    pStationary(buffer, skips)
                    skips = 0
                }
                pLowResToHighRes(buffer, other)
            } else if (other.lowResMovementBuffer.isReadable()) {
                if (skips > 0) {
                    pStationary(buffer, skips)
                    skips = 0
                }
                buffer.pBits(1, 1)
                buffer.pBits(other.lowResMovementBuffer)
            }
        }
        if (skips > 0) {
            pStationary(buffer, skips)
        }
    }

    private fun pLowResToHighRes(
        buffer: BitBuf,
        other: PlayerInfo,
    ) {
        val index = other.localIndex
        // buffer.pBits(1, 1)
        // buffer.pBits(2, 0)
        buffer.pBits(3, 1 shl 2)
        val lowResBuf = other.lowResMovementBuffer
        if (lowResBuf.isReadable()) {
            buffer.pBits(1, 1)
            buffer.pBits(lowResBuf)
        } else {
            buffer.pBits(1, 0)
        }
        // Can't merge this nicely as we need to exclude the last bit of both coords
        buffer.pBits(13, other.avatar.currentCoord.x)
        buffer.pBits(13, other.avatar.currentCoord.z)

        // Get a flags of all the extended info blocks that are 'outdated' to us and must be sent again.
        val extraFlags = other.extendedInfo.getLowToHighResChangeExtendedInfoFlags(extendedInfo)
        // Mark those flags as observer-dependent.
        observerExtendedInfoFlags.addFlag(index, extraFlags)
        stationary[index] = (stationary[index].toInt() or 0x2).toByte()
        highResolutionPlayers.set(index)
        val flag = other.extendedInfo.flags or observerExtendedInfoFlags.getFlag(index)
        val hasExtendedInfoBlock = flag != 0
        if (hasExtendedInfoBlock) {
            extendedInfoIndices[extendedInfoCount++] = index.toShort()
            buffer.pBits(1, 1)
        } else {
            buffer.pBits(1, 0)
        }
    }

    private fun processHighResolution(
        buffer: BitBuf,
        skipUnmodified: Boolean,
    ) {
        var skips = 0
        for (i in 0 until highResolutionCount) {
            val index = highResolutionIndices[i].toInt()
            val isUnmodified = (stationary[index].toInt() and 0x1) != 0
            if (skipUnmodified == isUnmodified) {
                continue
            }
            val other = protocol.getPlayerInfo(index)
            if (!isVisible(other)) {
                if (skips > 0) {
                    pStationary(buffer, skips)
                    skips = 0
                }
                pHighToLowResChange(buffer, index)
                continue
            }

            val flag = other.extendedInfo.flags or observerExtendedInfoFlags.getFlag(index)
            val hasExtendedInfoBlock = flag != 0
            val highResBuf = other.highResMovementBuffer
            val skipped = !hasExtendedInfoBlock && !highResBuf.isReadable()
            if (!skipped) {
                if (skips > 0) {
                    pStationary(buffer, skips)
                    skips = 0
                }
                pHighRes(buffer, index, hasExtendedInfoBlock, highResBuf)
                continue
            }
            skips++
            stationary[index] = (stationary[index].toInt() or 0x2).toByte()
        }
        if (skips > 0) {
            pStationary(buffer, skips)
        }
    }

    private fun pStationary(
        buffer: BitBuf,
        count: Int,
    ) {
        buffer.pBits(1, 0)
        // We subtract one as the protocol naturally expects one to be skipped
        // if the skip block is written in the first place
        val countMinusOne = count - 1
        if (countMinusOne == 0) {
            buffer.pBits(2, 0)
        } else if (countMinusOne <= 0x1F) {
            // buffer.pBits(2, 1)
            // buffer.pBits(5, countMinusOne)
            buffer.pBits(7, 1 shl 5 or countMinusOne)
        } else if (countMinusOne <= 0xFF) {
            // buffer.pBits(2, 2)
            // buffer.pBits(8, countMinusOne)
            buffer.pBits(10, 2 shl 8 or countMinusOne)
        } else {
            // buffer.pBits(2, 3)
            // buffer.pBits(11, countMinusOne)
            buffer.pBits(13, 3 shl 11 or countMinusOne)
        }
    }

    private fun pHighRes(
        buffer: BitBuf,
        index: Int,
        extendedInfo: Boolean,
        highResBuf: UnsafeLongBackedBitBuf,
    ): Boolean {
        // is not skipped
        buffer.pBits(1, 1)
        // has extended info?
        if (extendedInfo) {
            extendedInfoIndices[extendedInfoCount++] = index.toShort()
            buffer.pBits(1, 1)
        } else {
            buffer.pBits(1, 0)
        }
        if (highResBuf.isReadable()) {
            // movement
            buffer.pBits(highResBuf)
        } else {
            // no movement
            buffer.pBits(2, 0)
        }
        return true
    }

    private fun pHighToLowResChange(
        buffer: BitBuf,
        index: Int,
    ) {
        highResolutionPlayers.set(index, false)
        // buffer.pBits(1, 1)
        // buffer.pBits(1, 0)
        // buffer.pBits(2, 0)
        buffer.pBits(4, 1 shl 3)
    }

    @OptIn(ExperimentalContracts::class)
    private fun isVisible(other: PlayerInfo?): Boolean {
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
        val curCoord = this.avatar.currentCoord
        val otherCoord = other.avatar.currentCoord
        return curCoord.inDistance(otherCoord, this.avatar.resizeRange)
    }

    private fun allocBuffer(): ByteBuf {
        // Acquire a new buffer with each cycle, in case the previous one isn't fully written out yet
        val buffer = allocator.buffer(BUF_CAPACITY, BUF_CAPACITY)
        this.buffer = buffer
        return buffer
    }

    /**
     * Reset any temporary properties from this cycle.
     * Any extended information which doesn't require caching would also be cleared out at this stage.
     */
    internal fun postUpdate() {
        this.avatar.postUpdate()
        extendedInfo.reset()
        lowResolutionCount = 0
        highResolutionCount = 0
        // Only need to reset the count here, the actual numbers don't matter.
        extendedInfoCount = 0
        for (i in 1 until capacity) {
            stationary[i] = (stationary[i].toInt() shr 1).toByte()
            if (highResolutionPlayers.get(i)) {
                highResolutionIndices[highResolutionCount++] = i.toShort()
            } else {
                lowResolutionIndices[lowResolutionCount++] = i.toShort()
            }
        }
        observerExtendedInfoFlags.reset()
        extendedInfo.reset()
    }

    override fun onAlloc() {
        avatar.reset()
    }

    override fun onDealloc() {
    }

    internal fun reset() {
        // TODO: Reset all the properties properly
        lowResolutionIndices.fill(0)
        lowResolutionCount = 0
        highResolutionIndices.fill(0)
        highResolutionCount = 0
        extendedInfoCount = 0
        extendedInfoIndices.fill(0)
        extendedInfo.reset()
    }

    private fun prepareLowResMovement(
        globalLowResolutionPositionRepository: GlobalLowResolutionPositionRepository,
    ): UnsafeLongBackedBitBuf {
        val old = globalLowResolutionPositionRepository.getPreviousLowResolutionPosition(localIndex)
        val cur = globalLowResolutionPositionRepository.getCurrentLowResolutionPosition(localIndex)
        if (old == cur) {
            return this.lowResMovementBuffer.clear()
        }
        val buffer = this.lowResMovementBuffer.clear()
        val deltaX = cur.x - old.x
        val deltaZ = cur.z - old.z
        val deltaLevel = cur.level - old.level
        if (deltaX == 0 && deltaZ == 0) {
            buffer.pBits(2, 1)
            buffer.pBits(2, deltaLevel)
        } else if (abs(deltaX) <= 1 && abs(deltaZ) <= 1) {
            buffer.pBits(2, 2)
            buffer.pBits(2, deltaLevel)
            buffer.pBits(3, CellOpcodes.singleCellMovementOpcode(deltaX, deltaZ))
        } else {
            buffer.pBits(2, 3)
            buffer.pBits(2, deltaLevel)
            buffer.pBits(8, deltaX and 0xFF)
            buffer.pBits(8, deltaZ and 0xFF)
        }
        return buffer
    }

    private fun prepareHighResMovement(): UnsafeLongBackedBitBuf {
        val oldCoord = avatar.lastCoord
        val newCoord = avatar.currentCoord
        if (oldCoord == newCoord) {
            return this.highResMovementBuffer.clear()
        }
        val buffer = this.highResMovementBuffer.clear()
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

    private fun pWalk(
        buffer: UnsafeLongBackedBitBuf,
        deltaX: Int,
        deltaZ: Int,
    ) {
        buffer.pBits(2, 1)
        buffer.pBits(3, CellOpcodes.singleCellMovementOpcode(deltaX, deltaZ))
    }

    private fun pRun(
        buffer: UnsafeLongBackedBitBuf,
        deltaX: Int,
        deltaZ: Int,
    ) {
        buffer.pBits(2, 2)
        buffer.pBits(4, CellOpcodes.dualCellMovementOpcode(deltaX, deltaZ))
    }

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
        private const val BUF_CAPACITY: Int = 40_000
    }
}
