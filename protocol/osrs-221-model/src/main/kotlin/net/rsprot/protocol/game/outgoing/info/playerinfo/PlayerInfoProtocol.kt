package net.rsprot.protocol.game.outgoing.info.playerinfo

import io.netty.buffer.ByteBufAllocator
import net.rsprot.compression.HuffmanCodec
import net.rsprot.protocol.game.outgoing.info.playerinfo.util.LowResolutionPosition
import net.rsprot.protocol.game.outgoing.info.worker.DefaultProtocolWorker
import net.rsprot.protocol.game.outgoing.info.worker.ProtocolWorker
import net.rsprot.protocol.internal.game.outgoing.info.encoder.ExtendedInfoEncoders
import net.rsprot.protocol.shared.platform.PlatformType
import java.util.concurrent.Callable

public class PlayerInfoProtocol(
    private val capacity: Int,
    private val allocator: ByteBufAllocator,
    private val worker: ProtocolWorker = DefaultProtocolWorker(),
    extendedInfoEncoders: Map<PlatformType, ExtendedInfoEncoders>,
    huffmanCodec: HuffmanCodec,
) {
    private val lowResolutionPositionRepository: GlobalLowResolutionPositionRepository =
        GlobalLowResolutionPositionRepository(
            capacity,
        )
    private val playerInfoRepository: PlayerInfoRepository =
        PlayerInfoRepository(capacity) { localIndex, platformType ->
            PlayerInfo(
                this,
                localIndex,
                capacity,
                allocator,
                platformType,
                extendedInfoEncoders,
                huffmanCodec,
            )
        }
    private val callables: MutableList<Callable<Unit>> = ArrayList(capacity)

    internal fun getPlayerInfo(idx: Int): PlayerInfo? {
        return playerInfoRepository.getOrNull(idx)
    }

    public fun alloc(
        idx: Int,
        platformType: PlatformType,
    ): PlayerInfo {
        return playerInfoRepository.alloc(idx, platformType)
    }

    internal fun getLowResolutionPosition(idx: Int): LowResolutionPosition {
        return lowResolutionPositionRepository.getCurrentLowResolutionPosition(idx)
    }

    public fun prepare() {
        // Synchronize the known low res positions of everyone for this cycle
        for (i in 1..<capacity) {
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

    public fun putBitcodes() {
        execute {
            pBitcodes()
        }
    }

    public fun prepareExtendedInfo() {
        execute {
            precomputeExtendedInfo()
        }
    }

    public fun putExtendedInfo() {
        execute {
            putExtendedInfo()
        }
    }

    public fun postUpdate() {
        execute {
            postUpdate()
        }
        lowResolutionPositionRepository.postUpdate()
    }

    private inline fun execute(crossinline block: PlayerInfo.() -> Unit) {
        for (i in 1..<capacity) {
            val info = playerInfoRepository.getOrNull(i) ?: continue
            callables += Callable { block(info) }
        }
        worker.execute(callables)
        callables.clear()
    }
}
