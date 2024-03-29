package net.rsprot.protocol.game.outgoing.info.playerinfo

import io.netty.buffer.ByteBufAllocator
import net.rsprot.compression.HuffmanCodec
import net.rsprot.protocol.game.outgoing.info.playerinfo.util.LowResolutionPosition
import net.rsprot.protocol.internal.game.outgoing.info.encoder.ExtendedInfoEncoders
import net.rsprot.protocol.shared.platform.PlatformType

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
        // TODO: A thread pool supplier for asynchronous computation support

        /*val jobs = ArrayList<Callable<Unit>>(playerInfoRepository.wrapped.capacity())
        for (i in 1..<capacity) {
            jobs +=
                Callable {
                    playerInfoRepository.wrapped.getOrNull(i)?.pBitcodes()
                }
        }
        ForkJoinPool.commonPool().invokeAll(jobs)*/
        for (i in 1..<capacity) {
            playerInfoRepository.wrapped.getOrNull(i)?.pBitcodes()
        }
    }

    public fun prepareExtendedInfo() {
        for (i in 1..<capacity) {
            playerInfoRepository.wrapped.getOrNull(i)?.precomputeExtendedInfo()
        }
    }

    public fun putExtendedInfo() {
        for (i in 1..<capacity) {
            playerInfoRepository.wrapped.getOrNull(i)?.putExtendedInfo()
        }
    }

    public fun postUpdate() {
        for (i in 1..<capacity) {
            playerInfoRepository.wrapped.getOrNull(i)?.postUpdate()
        }
        lowResolutionPositionRepository.postUpdate()
    }
}
