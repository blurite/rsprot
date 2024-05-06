package net.rsprot.protocol.game.outgoing.info.npcinfo

import com.github.michaelbull.logging.InlineLogger
import io.netty.buffer.ByteBufAllocator
import net.rsprot.protocol.common.client.ClientTypeMap
import net.rsprot.protocol.common.client.OldSchoolClientType
import net.rsprot.protocol.common.game.outgoing.info.npcinfo.encoder.NpcResolutionChangeEncoder
import net.rsprot.protocol.game.outgoing.info.worker.DefaultProtocolWorker
import net.rsprot.protocol.game.outgoing.info.worker.ProtocolWorker
import java.util.concurrent.Callable

/**
 * NPC info protocol is the root class bringing everything together about NPC info.
 * @property allocator the byte buffer allocator used for pre-computing bit codes and
 * extended info blocks.
 * @property npcIndexSupplier the interface that supplies indices of NPCs near the player
 * that need to be added to the high resolution view.
 * @property resolutionChangeEncoders a client-specific map of resolution change encoders,
 * as the low to high resolution change is scrambled between clients and revision,
 * it needs to be supplied by the respective client module.
 * @param avatarFactory the factory responsible for allocating new npc avatars.
 * @property worker the protocol worker used to execute the jobs involved with
 * npc info computations.
 */
@Suppress("DuplicatedCode")
@ExperimentalUnsignedTypes
public class NpcInfoProtocol(
    private val allocator: ByteBufAllocator,
    private val npcIndexSupplier: NpcIndexSupplier,
    private val resolutionChangeEncoders: ClientTypeMap<NpcResolutionChangeEncoder>,
    avatarFactory: NpcAvatarFactory,
    private val exceptionHandler: NpcAvatarExceptionHandler,
    private val worker: ProtocolWorker = DefaultProtocolWorker(),
) {
    /**
     * The avatar repository keeps track of all the avatars currently in the game.
     */
    private val avatarRepository = avatarFactory.avatarRepository

    /**
     * Npc info repository keeps track of the main npc info objects which are allocated
     * by players at a 1:1 ratio.
     */
    private val npcInfoRepository: NpcInfoRepository =
        NpcInfoRepository { localIndex, clientType ->
            NpcInfo(
                allocator,
                avatarRepository,
                clientType,
                localIndex,
                npcIndexSupplier,
                resolutionChangeEncoders,
            )
        }

    /**
     * The list of [Callable] instances which perform the jobs for player info.
     * This list itself is re-used throughout the lifespan of the application,
     * but the [Callable] instances themselves are generated for every job.
     */
    private val callables: MutableList<Callable<Unit>> = ArrayList(PROTOCOL_CAPACITY)

    /**
     * Allocates a new npc info object, or re-uses an older one if possible.
     * @param idx the index of the player allocating the npc info object.
     * @param oldSchoolClientType the client on which the player has logged into.
     */
    public fun alloc(
        idx: Int,
        oldSchoolClientType: OldSchoolClientType,
    ): NpcInfo {
        return npcInfoRepository.alloc(idx, oldSchoolClientType)
    }

    /**
     * Deallocates the provided npc info object, allowing it to be used up
     * by another player in the future.
     * @param info the npc info object to deallocate
     */
    public fun dealloc(info: NpcInfo) {
        npcInfoRepository.dealloc(info.localPlayerIndex)
    }

    /**
     * Gets the npc info at the provided index.
     * @param idx the index of the npc info
     * @return npc info object at that index
     * @throws IllegalStateException if the npc info is null at that index
     * @throws ArrayIndexOutOfBoundsException if the index is out of bounds
     */
    public operator fun get(idx: Int): NpcInfo {
        return npcInfoRepository[idx]
    }

    /**
     * Updates the npc info protocol for this cycle.
     * The jobs here will be executed according to the [worker] specified,
     * allowing multithreaded execution if selected.
     */
    public fun update() {
        prepareBitcodes()
        putBitcodes()
        prepareExtendedInfo()
        putExtendedInfo()
        postUpdate()
    }

    /**
     * Prepares the high resolution bitcodes of all the NPC avatars which have
     * at least one observer.
     */
    private fun prepareBitcodes() {
        for (i in 0..<NpcAvatarRepository.AVATAR_CAPACITY) {
            val avatar = avatarRepository.getOrNull(i) ?: continue
            if (!avatar.hasObservers()) continue
            try {
                avatar.prepareBitcodes()
            } catch (e: Exception) {
                exceptionHandler.exceptionCaught(i, e)
            } catch (t: Throwable) {
                logger.error(t) {
                    "Error during npc bitcode preparation"
                }
                throw t
            }
        }
    }

    /**
     * Precomputes the extended info blocks of all the NPCs which have at least one
     * observer (after calculating all the bitcodes, to ensure any new additions are included).
     * Extended info blocks such as hits will still be computed on-demand though.
     */
    private fun prepareExtendedInfo() {
        for (i in 0..<NpcAvatarRepository.AVATAR_CAPACITY) {
            val avatar = avatarRepository.getOrNull(i) ?: continue
            if (!avatar.hasObservers()) continue
            try {
                avatar.extendedInfo.precompute()
            } catch (e: Exception) {
                exceptionHandler.exceptionCaught(i, e)
            } catch (t: Throwable) {
                logger.error(t) {
                    "Error during npc extended info preparation"
                }
                throw t
            }
        }
    }

    /**
     * Writes the bitcodes of npc info objects over into the buffer.
     * The work is split across according to the [worker] specified.
     */
    private fun putBitcodes() {
        execute {
            compute()
        }
    }

    /**
     * Writes the extended info blocks over into the buffer.
     * The work is split across according to the [worker] specified.
     */
    private fun putExtendedInfo() {
        execute {
            putExtendedInfo()
        }
    }

    /**
     * Cleans up any single-cycle temporary information for npc info protocol.
     */
    private fun postUpdate() {
        for (i in 1..<PROTOCOL_CAPACITY) {
            val info = npcInfoRepository.getOrNull(i) ?: continue
            info.afterUpdate()
        }
        for (i in 0..<65536) {
            val avatar = avatarRepository.getOrNull(i) ?: continue
            avatar.postUpdate()
        }
        avatarRepository.transferAvatars()
    }

    /**
     * Executes an inline [block] using strategies defined by the [worker].
     * This function will generate a new [Callable] instance for every
     * avatar in the world, which is then handed off to the [worker]
     * to execute with its preferred threading logic.
     * @param block the higher order function to execute within the [worker],
     * on each avatar.
     */
    private inline fun execute(crossinline block: NpcInfo.() -> Unit) {
        for (i in 1..<PROTOCOL_CAPACITY) {
            val info = npcInfoRepository.getOrNull(i) ?: continue
            callables +=
                Callable {
                    try {
                        block(info)
                    } catch (e: Exception) {
                        catchException(i, e)
                    } catch (t: Throwable) {
                        logger.error(t) {
                            "Error during npc updating"
                        }
                        throw t
                    }
                }
        }
        worker.execute(callables)
        callables.clear()
    }

    /**
     * Submits an exception to a specific player's npc info packet, which will be propagated further
     * whenever the server tries to call the [NpcInfo.toNpcInfoPacket] function, allowing the server to properly
     * handle exceptions for a given player despite it being calculated for the entire server in one go.
     * @param index the index of the player who caught an exception during their npc info processing
     * @param exception the exception caught during processing
     */
    private fun catchException(
        index: Int,
        exception: Exception,
    ) {
        val info = npcInfoRepository.getOrNull(index) ?: return
        npcInfoRepository.destroy(index)
        info.exception = exception
    }

    public companion object {
        /**
         * The maximum number of players in a world.
         */
        public const val PROTOCOL_CAPACITY: Int = 2048
        private val logger: InlineLogger = InlineLogger()
    }
}
