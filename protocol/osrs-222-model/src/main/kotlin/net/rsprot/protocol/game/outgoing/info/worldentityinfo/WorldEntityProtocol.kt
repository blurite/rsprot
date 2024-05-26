package net.rsprot.protocol.game.outgoing.info.worldentityinfo

import com.github.michaelbull.logging.InlineLogger
import io.netty.buffer.ByteBufAllocator
import net.rsprot.protocol.game.outgoing.info.worker.DefaultProtocolWorker
import net.rsprot.protocol.game.outgoing.info.worker.ProtocolWorker
import java.util.concurrent.Callable

public class WorldEntityProtocol(
    private val allocator: ByteBufAllocator,
    private val worker: ProtocolWorker = DefaultProtocolWorker(),
    private val indexSupplier: WorldEntityIndexSupplier,
    private val exceptionHandler: WorldEntityExceptionHandler,
    factory: WorldEntityAvatarFactory,
) {
    private val avatarRepository = factory.avatarRepository
    private val worldEntityRepository: WorldEntityRepository =
        WorldEntityRepository { localIndex, clientType ->
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

    public fun update() {
        prepareHighResolutionBuffers()
        updateInfos()
        postUpdate()
    }

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

    private fun updateInfos() {
        execute {
            updateWorldEntities()
        }
    }

    private fun postUpdate() {
        for (i in 1..<CAPACITY) {
            val info = worldEntityRepository.getOrNull(i) ?: continue
            info.afterUpdate()
        }
        for (i in 0..<CAPACITY) {
            val avatar = avatarRepository.getOrNull(i) ?: continue
            avatar.postUpdate()
        }
        avatarRepository.transferAvatars()
    }

    private inline fun execute(crossinline block: WorldEntityInfo.() -> Unit) {
        for (i in 1..<CAPACITY) {
            val info = worldEntityRepository.getOrNull(i) ?: continue
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

    private fun catchException(
        index: Int,
        exception: Exception,
    ) {
        val info = worldEntityRepository.getOrNull(index) ?: return
        worldEntityRepository.destroy(index)
        info.exception = exception
    }

    public companion object {
        public const val CAPACITY: Int = 2048
        private val logger: InlineLogger = InlineLogger()
    }
}
