package net.rsprot.protocol.game.outgoing.info.playerinfo

import io.netty.buffer.ByteBufAllocator
import net.rsprot.protocol.game.outgoing.info.playerinfo.util.LowResolutionPosition
import net.rsprot.protocol.internal.game.outgoing.info.encoder.ExtendedInfoEncoders
import net.rsprot.protocol.shared.platform.PlatformType

public class PlayerInfoProtocol(
    private val capacity: Int,
    private val allocator: ByteBufAllocator,
    extendedInfoEncoders: Map<PlatformType, ExtendedInfoEncoders>,
) {
    private val lowResolutionPositionRepository: GlobalLowResolutionPositionRepository =
        GlobalLowResolutionPositionRepository(
            capacity,
        )
    private val playerInfoRepository: PlayerInfoRepository =
        PlayerInfoRepository(capacity) { localIndex ->
            PlayerInfo(
                this,
                localIndex,
                capacity,
                allocator,
                extendedInfoEncoders,
            )
        }

    internal fun getPlayerInfo(idx: Int): PlayerInfo? {
        return playerInfoRepository.wrapped.getOrNull(idx)
    }

    public fun alloc(idx: Int): PlayerInfo {
        return playerInfoRepository.wrapped.alloc(idx)
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
    }

    public fun putExtendedInfo() {
    }

    public fun postUpdate() {
        for (i in 1..<capacity) {
            playerInfoRepository.wrapped.getOrNull(i)?.postUpdate()
        }
        lowResolutionPositionRepository.postUpdate()
    }
}
