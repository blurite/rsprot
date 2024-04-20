package net.rsprot.protocol.game.outgoing.info.npcinfo

import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufAllocator
import net.rsprot.buffer.bitbuffer.BitBuf
import net.rsprot.buffer.bitbuffer.toBitBuf
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprot.protocol.game.outgoing.info.playerinfo.util.ObserverExtendedInfoFlags
import net.rsprot.protocol.game.outgoing.info.util.ReferencePooledObject
import net.rsprot.protocol.internal.game.outgoing.info.CoordGrid
import net.rsprot.protocol.internal.game.outgoing.info.npcinfo.encoder.NpcResolutionChangeEncoder
import net.rsprot.protocol.internal.platform.PlatformMap
import net.rsprot.protocol.message.OutgoingMessage
import net.rsprot.protocol.shared.platform.PlatformType
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

@Suppress("ReplaceUntilWithRangeUntil")
@ExperimentalUnsignedTypes
public class NpcInfo internal constructor(
    private val allocator: ByteBufAllocator,
    private val repository: NpcAvatarRepository,
    private var platformType: PlatformType,
    private var localPlayerIndex: Int,
    private val indexSupplier: NpcIndexSupplier,
    private val lowResolutionToHighResolutionEncoders: PlatformMap<NpcResolutionChangeEncoder>,
) : ReferencePooledObject, OutgoingMessage {
    private var localPlayerLastCoord: CoordGrid = CoordGrid.INVALID
    private var localPlayerCurrentCoord: CoordGrid = CoordGrid.INVALID
    private var viewDistance: Int = MAX_SMALL_PACKET_DISTANCE
    private var highResolutionNpcIndices: UShortArray =
        UShortArray(MAX_HIGH_RESOLUTION_NPCS) {
            UNSIGNED_MAX_SHORT
        }
    private var temporaryHighResolutionNpcIndices: UShortArray =
        UShortArray(MAX_HIGH_RESOLUTION_NPCS) {
            UNSIGNED_MAX_SHORT
        }
    private var highResolutionNpcIndexCount: Int = 0

    /**
     * The extended info indices contain pointers to all the npcs for whom we need to
     * write an extended info block. We do this rather than directly writing them as this
     * improves CPU cache locality and allows us to batch extended info blocks together.
     */
    private val extendedInfoIndices: UShortArray = UShortArray(MAX_HIGH_RESOLUTION_NPCS)

    /**
     * The number of npcs for whom we need to write extended info blocks this cycle.
     */
    private var extendedInfoCount: Int = 0

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
    private val observerExtendedInfoFlags: ObserverExtendedInfoFlags =
        ObserverExtendedInfoFlags(MAX_HIGH_RESOLUTION_NPCS)

    private var buffer: ByteBuf? = null

    @Throws(IllegalStateException::class)
    public fun backingBuffer(): ByteBuf {
        return checkNotNull(buffer)
    }

    /**
     * Allocates a new buffer from the [allocator] with a capacity of [BUF_CAPACITY].
     * The old [buffer] will not be released, as that is the duty of the encoder class.
     */
    private fun allocBuffer(): ByteBuf {
        // Acquire a new buffer with each cycle, in case the previous one isn't fully written out yet
        val buffer = allocator.buffer(BUF_CAPACITY, BUF_CAPACITY)
        this.buffer = buffer
        return buffer
    }

    public fun updateCoord(
        level: Int,
        x: Int,
        z: Int,
    ) {
        this.localPlayerCurrentCoord =
            CoordGrid(
                level,
                x,
                z,
            )
    }

    public fun compute() {
        val viewDistance = this.viewDistance
        val buffer = allocBuffer()
        buffer.toBitBuf().use { bitBuffer ->
            val fragmented = processHighResolution(bitBuffer, viewDistance)
            if (fragmented) {
                defragmentIndices()
            }
            processLowResolution(bitBuffer, viewDistance)
            // Terminate the low-resolution processing block if there are extended info
            // blocks after that; if not, the loop ends naturally due to not enough
            // readable bits remaining (at most would have 7 bits remaining due to
            // the bit writer closing, which "finishes" the current byte).
            if (this.extendedInfoCount > 0) {
                bitBuffer.pBits(16, 0xFFFF)
            }
        }
    }

    public fun afterUpdate() {
        this.localPlayerLastCoord = localPlayerCurrentCoord
    }

    internal fun putExtendedInfo() {
        val jagBuffer = backingBuffer().toJagByteBuf()
        for (i in 0 until extendedInfoCount) {
            val index = extendedInfoIndices[i].toInt()
            val other = checkNotNull(repository.getOrNull(index))
            val observerFlag = observerExtendedInfoFlags.getFlag(i)
            other.extendedInfo.pExtendedInfo(
                platformType,
                jagBuffer,
                observerFlag,
                extendedInfoCount - i,
            )
        }
    }

    /**
     * Performs an index defragmentation on the [highResolutionNpcIndices] array.
     * This function will effectively take all indices that are NOT [UNSIGNED_MAX_SHORT]
     * and put them into the [temporaryHighResolutionNpcIndices] in a consecutive order,
     * without gaps. Afterwards, the [temporaryHighResolutionNpcIndices] and [highResolutionNpcIndices]
     * arrays get swapped out, so our [highResolutionNpcIndices] becomes a defragmented array.
     * This process occurs every cycle, after high resolution indices are processed, in order to
     * get rid of any gaps that were produced as a result of it.
     *
     * A breakdown of this process:
     * At the start of a cycle, we might have indices as `[1, 7, 5, 3, 8, 65535, ...]`
     * If we make the assumption that NPCs at indices 7 and 8 are being removed from our high resolution,
     * during the high resolution processing, npc at index 8 is dropped naturally - this is because
     * the client will automatically trim off any NPCs at the end which don't fit into the transmitted
     * count. So, npc at index 8 does not count towards fragmentation, as we just decrement the index count.
     * However, index 7, because it is in the middle of this array of indices, causes the array
     * to fragment. So in order to resolve this, we will iterate the fragmented indices
     * until we have collected [highResolutionNpcIndexCount] worth of valid indices into the
     * [temporaryHighResolutionNpcIndices] array.
     * After defragmenting, our array will look as `[1, 5, 3, 65535, ...]`.
     * While it is possible to do this with a single array, it requires one to shift every element
     * in the array after the first fragmentation occurs. As the arrays are relatively small, it's
     * better simply to use two arrays that get swapped every cycle, so we simply swap
     * the [temporaryHighResolutionNpcIndices] and [highResolutionNpcIndices] arrays between one another,
     * rather than needing to shift everything over.
     */
    private fun defragmentIndices() {
        var count = 0
        for (i in highResolutionNpcIndices.indices) {
            if (count >= highResolutionNpcIndexCount) {
                break
            }
            val index = highResolutionNpcIndices[i]
            if (index != UNSIGNED_MAX_SHORT) {
                temporaryHighResolutionNpcIndices[count++] = index
            }
        }
        val uncompressed = this.highResolutionNpcIndices
        this.highResolutionNpcIndices = this.temporaryHighResolutionNpcIndices
        this.temporaryHighResolutionNpcIndices = uncompressed
    }

    private fun processHighResolution(
        buffer: BitBuf,
        viewDistance: Int,
    ): Boolean {
        // If no one to process, skip
        if (this.highResolutionNpcIndexCount == 0) {
            buffer.pBits(8, 0)
            return false
        }
        // If our coordinate compared to last cycle changed more than 'viewDistance'
        // tiles, every NPC in our local view would be removed anyhow,
        // so by sending the count as 0, client automatically removes everyone
        if (isTooFar(viewDistance)) {
            buffer.pBits(8, 0)
            // While it would be more efficient to just... not do this block below,
            // the reality is there are ~25k static npcs in the game alone,
            // and by tracking the observer counts we can omit computing
            // extended info as well as high resolution movement blocks
            // for any npc that doesn't have a player near them,
            // which, even at full world, will be the majority of npcs.
            for (i in 0..<highResolutionNpcIndexCount) {
                val npcIndex = highResolutionNpcIndices[i].toInt()
                val avatar = repository.getOrNull(npcIndex) ?: continue
                avatar.removeObserver()
            }
            highResolutionNpcIndexCount = 0
            return false
        }
        // Iterate NPCs in a backwards order until the first npc who should not be removed
        // everyone else will be automatically dropped off by the client if the count
        // transmitted is less than what the client currently knows about
        for (i in highResolutionNpcIndexCount - 1 downTo 0) {
            val npcIndex = highResolutionNpcIndices[i].toInt()
            val avatar = repository.getOrNull(npcIndex)
            if (!removeHighResolutionNpc(avatar, viewDistance)) {
                break
            }
            avatar?.removeObserver()
            highResolutionNpcIndexCount--
        }
        val processedCount = this.highResolutionNpcIndexCount
        buffer.pBits(8, processedCount)
        for (i in 0..<processedCount) {
            val npcIndex = highResolutionNpcIndices[i].toInt()
            val avatar = repository.getOrNull(npcIndex)
            if (removeHighResolutionNpc(avatar, viewDistance)) {
                buffer.pBits(1, 1)
                buffer.pBits(2, 3)
                avatar?.removeObserver()
                highResolutionNpcIndexCount--
                continue
            }
            if (avatar.extendedInfo.flags != 0) {
                extendedInfoIndices[extendedInfoCount++] = npcIndex.toUShort()
            }
            val movementBuffer = checkNotNull(avatar.highResMovementBuffer)
            buffer.pBits(movementBuffer)
        }
        return processedCount != highResolutionNpcIndexCount
    }

    @OptIn(ExperimentalContracts::class)
    private fun removeHighResolutionNpc(
        avatar: NpcAvatar?,
        viewDistance: Int,
    ): Boolean {
        contract {
            returns(false) implies (avatar != null)
        }
        return avatar == null ||
            avatar.details.inaccessible ||
            avatar.details.isTeleporting() ||
            !withinDistance(localPlayerCurrentCoord, avatar.details.currentCoord, viewDistance)
    }

    private fun isTooFar(viewDistance: Int): Boolean {
        return !withinDistance(
            this.localPlayerLastCoord,
            this.localPlayerCurrentCoord,
            viewDistance,
        )
    }

    private fun processLowResolution(
        buffer: BitBuf,
        viewDistance: Int,
    ) {
        // If our local view is already maxed out, don't even request for indices
        if (this.highResolutionNpcIndexCount >= MAX_HIGH_RESOLUTION_NPCS) {
            return
        }
        val encoder = lowResolutionToHighResolutionEncoders[platformType]
        val largeDistance = viewDistance > MAX_SMALL_PACKET_DISTANCE
        val npcs =
            this.indexSupplier.supply(
                localPlayerIndex,
                localPlayerCurrentCoord.level,
                localPlayerCurrentCoord.x,
                localPlayerCurrentCoord.z,
                viewDistance,
            )
        while (npcs.hasNext()) {
            val index = npcs.next() and MAX_USHORT
            if (index == MAX_USHORT || isHighResolution(index)) {
                continue
            }
            if (this.highResolutionNpcIndexCount < MAX_HIGH_RESOLUTION_NPCS) {
                break
            }
            val avatar = repository.getOrNull(index) ?: continue
            avatar.addObserver()
            val i = highResolutionNpcIndexCount++
            highResolutionNpcIndices[i] = index.toUShort()
            val observerFlags = avatar.extendedInfo.getLowToHighResChangeExtendedInfoFlags()
            this.observerExtendedInfoFlags.addFlag(i, observerFlags)
            val extendedInfo = (avatar.extendedInfo.flags or observerFlags) != 0
            encoder.encode(
                buffer,
                avatar.details,
                extendedInfo,
                localPlayerCurrentCoord,
                largeDistance,
            )
        }
    }

    private fun isHighResolution(index: Int): Boolean {
        // NOTE: Perhaps it's more efficient to just allocate 65535 bits and do a bit check?
        // Would cost ~16.76mb at max world capacity
        for (i in 0..<highResolutionNpcIndexCount) {
            if (highResolutionNpcIndices[i].toInt() == index) {
                return true
            }
        }
        return false
    }

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
        platformType: PlatformType,
    ) {
        this.localPlayerIndex = index
        this.platformType = platformType
        this.localPlayerLastCoord = CoordGrid.INVALID
        this.viewDistance = MAX_SMALL_PACKET_DISTANCE
        this.highResolutionNpcIndexCount = 0
        this.extendedInfoCount = 0
        this.observerExtendedInfoFlags.reset()
    }

    override fun onDealloc() {
        this.buffer = null
    }

    private companion object {
        /**
         * The default capacity of the backing byte buffer into which all player info is written.
         */
        private const val BUF_CAPACITY: Int = 40_000
        private const val MAX_SMALL_PACKET_DISTANCE: Int = 15
        private const val MAX_HIGH_RESOLUTION_NPCS: Int = 250
        private const val UNSIGNED_MAX_SHORT: UShort = 0xFFFFu
        private const val MAX_USHORT = 0xFFFF
    }
}
