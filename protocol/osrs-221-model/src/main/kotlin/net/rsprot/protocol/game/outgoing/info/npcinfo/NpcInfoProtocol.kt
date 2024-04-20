package net.rsprot.protocol.game.outgoing.info.npcinfo

import io.netty.buffer.ByteBufAllocator
import net.rsprot.protocol.game.outgoing.info.worker.DefaultProtocolWorker
import net.rsprot.protocol.game.outgoing.info.worker.ProtocolWorker
import net.rsprot.protocol.internal.game.outgoing.info.npcinfo.encoder.NpcResolutionChangeEncoder
import net.rsprot.protocol.internal.platform.PlatformMap
import java.util.concurrent.Callable

@ExperimentalUnsignedTypes
public class NpcInfoProtocol(
    private val allocator: ByteBufAllocator,
    private val worker: ProtocolWorker = DefaultProtocolWorker(),
    private val npcIndexSupplier: NpcIndexSupplier,
    private val resolutionChangeEncoders: PlatformMap<NpcResolutionChangeEncoder>,
) {
    private val avatarRepository: NpcAvatarRepository = NpcAvatarRepository()

    private val npcInfoRepository: NpcInfoRepository =
        NpcInfoRepository { localIndex, platformType ->
            NpcInfo(
                allocator,
                avatarRepository,
                platformType,
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

    public fun compute() {
        prepareBitcodes()
        putBitcodes()
        prepareExtendedInfo()
        putExtendedInfo()
    }

    private fun prepareBitcodes() {
        for (i in 0..<NpcAvatarRepository.AVATAR_CAPACITY) {
            val avatar = avatarRepository.getOrNull(i) ?: continue
            if (!avatar.hasObservers()) continue
            avatar.prepareBitcodes()
        }
    }

    private fun prepareExtendedInfo() {
        for (i in 0..<NpcAvatarRepository.AVATAR_CAPACITY) {
            val avatar = avatarRepository.getOrNull(i) ?: continue
            if (!avatar.hasObservers()) continue
            avatar.extendedInfo.precompute()
        }
    }

    private fun putBitcodes() {
        execute {
            compute()
        }
    }

    private fun putExtendedInfo() {
        execute {
            putExtendedInfo()
        }
    }

    private fun postUpdate() {
        for (i in 1..<PROTOCOL_CAPACITY) {
            val info = npcInfoRepository.getOrNull(i) ?: continue
            info.afterUpdate()
        }
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
            callables += Callable { block(info) }
        }
        worker.execute(callables)
        callables.clear()
    }

    public companion object {
        public const val PROTOCOL_CAPACITY: Int = 2048
    }
}
