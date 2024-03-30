package net.rsprot.protocol.game.outgoing.info.playerinfo

import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufAllocator
import net.rsprot.compression.HuffmanCodec
import net.rsprot.protocol.game.outgoing.info.playerinfo.util.LowResolutionPosition
import net.rsprot.protocol.game.outgoing.info.worker.DefaultProtocolWorker
import net.rsprot.protocol.game.outgoing.info.worker.ProtocolWorker
import net.rsprot.protocol.internal.game.outgoing.info.encoder.ExtendedInfoEncoders
import net.rsprot.protocol.shared.platform.PlatformType
import java.util.concurrent.Callable
import java.util.concurrent.ForkJoinPool

/**
 * The player info protocol is responsible for tracking everything player info related
 * within the given world. This class holds every avatar, their state, and provides
 * means to allocate new player info instances.
 * @param allocator the [ByteBuf] allocator responsible for allocating the primary buffer
 * the is written out to the pipeline, as well as any intermediate buffers used by extended
 * info blocks. The allocator should ideally be pooled, as we acquire a new instance with each
 * cycle. This is because there isn't necessarily a guarantee that Netty threads have fully
 * written the information out to the network by the time the next cycle comes along and starts
 * writing into this buffer. A direct implementation is also preferred, as this avoids unnecessary
 * copying from and to the heap.
 * @param worker the worker responsible for executing the blocks of code found in player info.
 * The default worker will remain single-threaded if there are less than `coreCount * 4` players
 * in the world. Otherwise, it will use [ForkJoinPool] to execute these jobs. Both of these
 * are configurable within the [DefaultProtocolWorker] constructor.
 * @param extendedInfoEncoders a map of platform type to a wrapper of extended info encoders.
 * This map must provide encoders for all platform types that will be in use.
 * It is also worth noting that pre-computations for extended info blocks will be done
 * for all platforms if multiple platforms are registered.
 * @param huffmanCodec the huffman codec responsible for compressing public chat extended info block.
 */
public class PlayerInfoProtocol(
    private val allocator: ByteBufAllocator,
    private val worker: ProtocolWorker = DefaultProtocolWorker(),
    extendedInfoEncoders: Map<PlatformType, ExtendedInfoEncoders>,
    huffmanCodec: HuffmanCodec,
) {
    /**
     * The repository responsible for keeping track of all the players' low resolution
     * position within the world.
     */
    private val lowResolutionPositionRepository: GlobalLowResolutionPositionRepository =
        GlobalLowResolutionPositionRepository()

    /**
     * The repository responsible for allocating and storing player info instances of
     * all the avatars that exist.
     */
    private val playerInfoRepository: PlayerInfoRepository =
        PlayerInfoRepository { localIndex, platformType ->
            PlayerInfo(
                this,
                localIndex,
                allocator,
                platformType,
                extendedInfoEncoders,
                huffmanCodec,
            )
        }

    /**
     * The list of [Callable] instances which perform the jobs for player info.
     * This list itself is re-used throughout the lifespan of the application,
     * but the [Callable] instances themselves are generated for every job.
     */
    private val callables: MutableList<Callable<Unit>> = ArrayList(PROTOCOL_CAPACITY)

    /**
     * Gets the current element at index [idx], or null if it doesn't exist.
     * @param idx the index of the player info object to obtain
     * @throws ArrayIndexOutOfBoundsException if the index is below zero,
     * or above [PlayerInfoProtocol.PROTOCOL_CAPACITY].
     */
    @Throws(ArrayIndexOutOfBoundsException::class)
    internal fun getPlayerInfo(idx: Int): PlayerInfo? {
        return playerInfoRepository.getOrNull(idx)
    }

    /**
     * Allocates a new player info instance at index [idx]
     * @param idx the index of the player to allocate
     * @param platformType the platform on which this player is.
     * The platform type is used to determine which extended info encoders to utilize
     * when building the buffers for this packet.
     * @throws ArrayIndexOutOfBoundsException if the [idx] is below 1, or above 2047.
     * @throws IllegalStateException if the element at index [idx] is already in use.
     */
    @Throws(
        ArrayIndexOutOfBoundsException::class,
        IllegalStateException::class,
    )
    public fun alloc(
        idx: Int,
        platformType: PlatformType,
    ): PlayerInfo {
        // Only handle index 0 as a special case, as the protocol
        // does not allow putting an avatar at index 0.
        // Other index exceptions are handled by the alloc function.
        if (idx == 0) {
            throw ArrayIndexOutOfBoundsException("Index 0 is not valid for player info protocol.")
        }
        return playerInfoRepository.alloc(idx, platformType)
    }

    /**
     * Gets the current cycle's low resolution position of the player at index [idx].
     * @param idx the index of the player
     * @return the low resolution position of that player in the current cycle.
     */
    internal fun getLowResolutionPosition(idx: Int): LowResolutionPosition {
        return lowResolutionPositionRepository.getCurrentLowResolutionPosition(idx)
    }

    /**
     * Prepares the player info protocol for every player in the world.
     * First it will synchronize the low resolution positions of all the avatars in the world.
     * Afterwards, according to the implementation defined by the [worker],
     * this will cache the low and high resolution movement bit buffers
     * for every avatar in the world.
     */
    public fun prepare() {
        // Synchronize the known low res positions of everyone for this cycle
        for (i in 1..<PROTOCOL_CAPACITY) {
            val info = playerInfoRepository.getOrNull(i)
            if (info == null) {
                lowResolutionPositionRepository.markUnused(i)
            } else {
                lowResolutionPositionRepository.update(i, info.avatar.currentCoord)
            }
        }
        execute {
            prepareBitcodes(lowResolutionPositionRepository)
        }
    }

    /**
     * Writes the bitcodes for each avatar in the world according to the implementation
     * defined by the [worker].
     */
    public fun putBitcodes() {
        execute {
            pBitcodes()
        }
    }

    /**
     * Prepares the extended info blocks for all the avatars in the world
     * according to the implementation defined by the [worker].
     * This function will generate cached variants of extended info blocks
     * using the [allocator] for a means of providing these buffers.
     * Only cacheable extended info blocks which were flagged globally
     * will be generated. Anything else, however, will be computed on demand
     * without an intermediate byte buffer.
     */
    public fun prepareExtendedInfo() {
        execute {
            precomputeExtendedInfo()
        }
    }

    /**
     * Writes the extended info blocks for all the avatars in the world according to the
     * implementation defined by the [worker].
     * This function will mostly perform byte buffer memory copying,
     * as most of the extended info blocks will be cached.
     * If direct buffers are used, fast native memory copy invocations will be used.
     */
    public fun putExtendedInfo() {
        execute {
            putExtendedInfo()
        }
    }

    /**
     * Cleans up the player info protocol for all the avatars in the world,
     * according to the implementation defined by the [worker].
     */
    public fun postUpdate() {
        execute {
            postUpdate()
        }
        lowResolutionPositionRepository.postUpdate()
    }

    /**
     * Executes an inline [block] using strategies defined by the [worker].
     * This function will generate a new [Callable] instance for every
     * avatar in the world, which is then handed off to the [worker]
     * to execute with its preferred threading logic.
     * @param block the higher order function to execute within the [worker],
     * on each avatar.
     */
    private inline fun execute(crossinline block: PlayerInfo.() -> Unit) {
        for (i in 1..<PROTOCOL_CAPACITY) {
            val info = playerInfoRepository.getOrNull(i) ?: continue
            callables += Callable { block(info) }
        }
        worker.execute(callables)
        callables.clear()
    }

    public companion object {
        /**
         * The maximum capacity of the player info protocol.
         * This constant is used for the protocol's own functionality, as the client
         * tends to iterate up to 2048, we cannot often allocate smaller data structures,
         * as that would break the client's logic.
         */
        public const val PROTOCOL_CAPACITY: Int = 2048
    }
}
