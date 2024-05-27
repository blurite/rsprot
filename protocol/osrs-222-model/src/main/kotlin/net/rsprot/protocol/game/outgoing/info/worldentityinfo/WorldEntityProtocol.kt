package net.rsprot.protocol.game.outgoing.info.worldentityinfo

import com.github.michaelbull.logging.InlineLogger
import io.netty.buffer.ByteBufAllocator
import net.rsprot.protocol.common.client.OldSchoolClientType
import net.rsprot.protocol.game.outgoing.info.worker.DefaultProtocolWorker
import net.rsprot.protocol.game.outgoing.info.worker.ProtocolWorker
import java.util.concurrent.Callable

/**
 * The world entity protocol class will track everything related to world entities.
 * @property allocator the byte buffer allocator used for world entity buffers.
 * @property indexSupplier the index supplier implementation that yields indices of
 * the world entities which are near a specific coordinate.
 * @property exceptionHandler the exception handler which will be notified whenever
 * there is an exception caught in world entity avatar pre-computation.
 * @param factory the avatar factory used to provide instances of world entity avatars.
 * @property worker the protocol worker that will be executing the computation
 * of avatar and info buffers on the thread(s) specified by the implementation.
 * @property avatarRepository the repository containing all the world entity avatars.
 * @property worldEntityInfoRepository the repository containing all the currently
 * in used world entity info instances.
 */
public class WorldEntityProtocol(
    private val allocator: ByteBufAllocator,
    private val indexSupplier: WorldEntityIndexSupplier,
    private val exceptionHandler: WorldEntityAvatarExceptionHandler,
    factory: WorldEntityAvatarFactory,
    private val worker: ProtocolWorker = DefaultProtocolWorker(),
) {
    private val avatarRepository = factory.avatarRepository
    private val worldEntityInfoRepository: WorldEntityInfoRepository =
        WorldEntityInfoRepository { localIndex, clientType ->
            WorldEntityInfo(
                localIndex,
                allocator,
                clientType,
                factory.avatarRepository,
                indexSupplier,
            )
        }

    /**
     * The list of [Callable] instances which perform the jobs for player info.
     * This list itself is re-used throughout the lifespan of the application,
     * but the [Callable] instances themselves are generated for every job.
     */
    private val callables: MutableList<Callable<Unit>> = ArrayList(CAPACITY)

    /**
     * Allocates a new instance of world entity info.
     * @param idx the index of the player who is requesting a world entity info.
     * @param oldSchoolClientType the client type on which the player has logged in.
     * @return an instance of the world entity info.
     */
    public fun alloc(
        idx: Int,
        oldSchoolClientType: OldSchoolClientType,
    ): WorldEntityInfo {
        return worldEntityInfoRepository.alloc(idx, oldSchoolClientType)
    }

    /**
     * Deallocates the world entity info, allowing for it to be re-used in the future.
     * @param info the world entity info to be deallocated.
     */
    public fun dealloc(info: WorldEntityInfo) {
        worldEntityInfoRepository.dealloc(info.localIndex)
    }

    /**
     * Updates all the world entities that exist in one go.
     */
    public fun update() {
        prepareHighResolutionBuffers()
        updateInfos()
        postUpdate()
    }

    /**
     * Pre-computes the high resolution block of world entities that exist.
     */
    private fun prepareHighResolutionBuffers() {
        for (i in 0..<CAPACITY) {
            val avatar = avatarRepository.getOrNull(i) ?: continue
            try {
                avatar.precompute()
            } catch (e: Exception) {
                exceptionHandler.exceptionCaught(i, e)
            } catch (t: Throwable) {
                logger.error(t) {
                    "Error during worldentity avatar computation"
                }
                throw t
            }
        }
    }

    /**
     * Builds the primary worldentity info buffers to be sent to all the players.
     */
    private fun updateInfos() {
        execute {
            updateWorldEntities()
        }
    }

    /**
     * Cleans up all the temporary state after world entity buffers have been computed.
     */
    private fun postUpdate() {
        for (i in 0..<CAPACITY) {
            val avatar = avatarRepository.getOrNull(i) ?: continue
            avatar.postUpdate()
        }
        avatarRepository.transferAvatars()
    }

    /**
     * Executes the provided [block] for every world entity info that currently exists,
     * using the [worker] implementation provided.
     * @param block the lambda block to execute for each of the world entity infos.
     */
    private inline fun execute(crossinline block: WorldEntityInfo.() -> Unit) {
        for (i in 1..<CAPACITY) {
            val info = worldEntityInfoRepository.getOrNull(i) ?: continue
            callables +=
                Callable {
                    try {
                        block(info)
                    } catch (e: Exception) {
                        catchException(i, e)
                    } catch (t: Throwable) {
                        logger.error(t) {
                            "Error during worldentity updating"
                        }
                        throw t
                    }
                }
        }
        worker.execute(callables)
        callables.clear()
    }

    /**
     * Catches and marks an exception for the provided player.
     * This exception will be thrown when the toPacket() call is performed
     * for that player's world entity info, allowing for it to be handled
     * properly at a per-player instance.
     */
    private fun catchException(
        index: Int,
        exception: Exception,
    ) {
        val info = worldEntityInfoRepository.getOrNull(index) ?: return
        worldEntityInfoRepository.destroy(index)
        info.exception = exception
    }

    public companion object {
        /**
         * The maximum number of world entities that can exist in the world.
         */
        public const val CAPACITY: Int = 2048

        /**
         * The logger used to notify about exceptions that may otherwise be lost.
         */
        private val logger: InlineLogger = InlineLogger()
    }
}
