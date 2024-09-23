package net.rsprot.protocol.game.outgoing.info.playerinfo

import com.github.michaelbull.logging.InlineLogger
import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufAllocator
import net.rsprot.protocol.common.client.OldSchoolClientType
import net.rsprot.protocol.game.outgoing.info.ByteBufRecycler
import net.rsprot.protocol.game.outgoing.info.playerinfo.util.LowResolutionPosition
import net.rsprot.protocol.game.outgoing.info.worker.DefaultProtocolWorker
import net.rsprot.protocol.game.outgoing.info.worker.ProtocolWorker
import java.util.concurrent.Callable
import java.util.concurrent.ForkJoinPool
import kotlin.Exception

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
 */
public class PlayerInfoProtocol(
    private val allocator: ByteBufAllocator,
    private val worker: ProtocolWorker = DefaultProtocolWorker(),
    private val avatarFactory: PlayerAvatarFactory,
) {
    /**
     * A recycler to ensure all buffers allocated by player info eventually get released.
     */
    private val recycler: ByteBufRecycler = ByteBufRecycler()

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
        PlayerInfoRepository { localIndex, clientType ->
            PlayerInfo(
                this,
                localIndex,
                allocator,
                clientType,
                avatarFactory.alloc(localIndex),
                recycler,
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
    internal fun getPlayerInfo(idx: Int): PlayerInfo? = playerInfoRepository.getOrNull(idx)

    /**
     * Allocates a new player info instance at index [idx]
     * @param idx the index of the player to allocate
     * @param oldSchoolClientType the client on which this player is.
     * The client type is used to determine which extended info encoders to utilize
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
        oldSchoolClientType: OldSchoolClientType,
    ): PlayerInfo {
        // Only handle index 0 as a special case, as the protocol
        // does not allow putting an avatar at index 0.
        // Other index exceptions are handled by the alloc function.
        if (idx == 0) {
            throw ArrayIndexOutOfBoundsException("Index 0 is not valid for player info protocol.")
        }
        return playerInfoRepository.alloc(idx, oldSchoolClientType)
    }

    /**
     * Deallocates the player info object, releasing it back into the pool to be used by another player.
     * @param info the player info object
     */
    public fun dealloc(info: PlayerInfo) {
        // Prevent returning a destroyed player info object back into the pool
        if (info.isDestroyed()) {
            return
        }
        playerInfoRepository.dealloc(info.localIndex)
    }

    /**
     * Gets the current cycle's low resolution position of the player at index [idx].
     * @param idx the index of the player
     * @return the low resolution position of that player in the current cycle.
     */
    internal fun getLowResolutionPosition(idx: Int): LowResolutionPosition =
        lowResolutionPositionRepository.getCurrentLowResolutionPosition(idx)

    public fun update() {
        prepare()
        putBitcodes()
        prepareExtendedInfo()
        putExtendedInfo()
        postUpdate()
        recycler.cycle()
        cycleCount++
    }

    /**
     * Prepares the player info protocol for every player in the world.
     * First it will synchronize the low resolution positions of all the avatars in the world.
     * Afterwards, according to the implementation defined by the [worker],
     * this will cache the low and high resolution movement bit buffers
     * for every avatar in the world.
     */
    private fun prepare() {
        // Synchronize the known low res positions of everyone for this cycle
        for (i in 1..<PROTOCOL_CAPACITY) {
            val info = playerInfoRepository.getOrNull(i)
            if (info == null) {
                lowResolutionPositionRepository.markUnused(i)
            } else {
                lowResolutionPositionRepository.update(i, info.avatar.currentCoord)
            }
        }
        for (i in 1..<PROTOCOL_CAPACITY) {
            try {
                playerInfoRepository.getOrNull(i)?.prepareBitcodes(lowResolutionPositionRepository)
            } catch (e: Exception) {
                catchException(i, e)
            } catch (t: Throwable) {
                logger.error(t) {
                    "Error during player updating preparation"
                }
                throw t
            }
        }
    }

    /**
     * Writes the bitcodes for each avatar in the world according to the implementation
     * defined by the [worker].
     */
    private fun putBitcodes() {
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
    private fun prepareExtendedInfo() {
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
    private fun putExtendedInfo() {
        execute {
            putExtendedInfo()
        }
    }

    /**
     * Cleans up the player info protocol for all the avatars in the world,
     * according to the implementation defined by the [worker].
     */
    private fun postUpdate() {
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
            callables +=
                Callable {
                    try {
                        block(info)
                    } catch (e: Exception) {
                        catchException(i, e)
                    } catch (t: Throwable) {
                        logger.error(t) {
                            "Error during player updating"
                        }
                        throw t
                    }
                }
        }
        worker.execute(callables)
        callables.clear()
    }

    /**
     * Submits an exception to a specific player's playerinfo packet, which will be propagated further
     * whenever the server tries to call the [PlayerInfo.toPacket] function, allowing the server to properly
     * handle exceptions for a given player despite it being calculated for the entire server in one go.
     * @param index the index of the player who caught an exception during their processing
     * @param exception the exception caught during processing
     */
    private fun catchException(
        index: Int,
        exception: Exception,
    ) {
        val info = playerInfoRepository.getOrNull(index) ?: return
        playerInfoRepository.destroy(index)
        info.exception = exception
    }

    public companion object {
        /**
         * The maximum capacity of the player info protocol.
         * This constant is used for the protocol's own functionality, as the client
         * tends to iterate up to 2048, we cannot often allocate smaller data structures,
         * as that would break the client's logic.
         */
        public const val PROTOCOL_CAPACITY: Int = 2048
        private val logger: InlineLogger = InlineLogger()

        /**
         * The number of Player info update cycles that have occurred.
         * We need to track this to avoid a nasty bug with servers de-allocating + re-allocating
         * an avatar on the same cycle, in a small area. The effective bug is that another player takes
         * ones' avatar, which leads to info protocol thinking nothing has changed (assuming the new player
         * is still within range of the old one, enough to be in high resolution).
         *
         * We solve this by forcibly removing a player from high resolution view if the avatar was
         * allocated on current cycle. If the player is still within range, they will be re-added
         * later on in the cycle via low resolution updates - but correctly this time around!
         */
        internal var cycleCount: Int = 0
    }
}
