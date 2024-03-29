package net.rsprot.protocol.game.outgoing.info.playerinfo

import io.netty.buffer.ByteBufAllocator
import net.rsprot.compression.HuffmanCodec
import net.rsprot.protocol.game.outgoing.info.playerinfo.util.LowResolutionPosition
import net.rsprot.protocol.internal.game.outgoing.info.encoder.ExtendedInfoEncoders
import net.rsprot.protocol.shared.platform.PlatformType
import java.util.concurrent.Callable
import java.util.concurrent.ForkJoinPool

public class PlayerInfoProtocol(
    private val capacity: Int,
    private val allocator: ByteBufAllocator,
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

    internal fun getPlayerInfo(idx: Int): PlayerInfo? {
        return playerInfoRepository.wrapped.getOrNull(idx)
    }

    public fun alloc(
        idx: Int,
        platformType: PlatformType,
    ): PlayerInfo {
        return playerInfoRepository.wrapped.alloc(idx, platformType)
    }

    public fun prepare() {
        // Synchronize the known low res positions of everyone for this cycle
        for (i in 1..<capacity) {
            val info = playerInfoRepository.wrapped.getOrNull(i)
            if (info == null) {
                lowResolutionPositionRepository.markUnused(i)
            } else {
                lowResolutionPositionRepository.update(i, info.avatar.currentCoord)
            }
        }
        for (i in 1..<capacity) {
            playerInfoRepository.wrapped.getOrNull(i)?.prepareBitcodes(lowResolutionPositionRepository)
        }
    }

    internal fun getLowResolutionPosition(idx: Int): LowResolutionPosition {
        return lowResolutionPositionRepository.getCurrentLowResolutionPosition(idx)
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

    private fun execute(block: PlayerInfo.() -> Unit) {
        // TODO: A thread pool supplier for asynchronous computation support
        if (ASYNC) {
            val jobs = ArrayList<Callable<Unit>>(2048)
            for (i in 1..<capacity) {
                val info = playerInfoRepository.wrapped.getOrNull(i) ?: continue
                jobs +=
                    Callable {
                        block(info)
                    }
            }
            ForkJoinPool.commonPool().invokeAll(jobs)
        } else {
            for (i in 1..<capacity) {
                val info = playerInfoRepository.wrapped.getOrNull(i) ?: continue
                block(info)
            }
        }
    }

    private companion object {
        private const val ASYNC: Boolean = true
    }
}
