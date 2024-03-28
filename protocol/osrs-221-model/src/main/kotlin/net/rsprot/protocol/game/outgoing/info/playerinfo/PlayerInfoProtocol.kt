package net.rsprot.protocol.game.outgoing.info.playerinfo

import io.netty.buffer.ByteBufAllocator
import net.rsprot.protocol.game.outgoing.info.playerinfo.util.LowResolutionPosition

public class PlayerInfoProtocol(
    private val capacity: Int,
    private val allocator: ByteBufAllocator,
) {
    private val mapSectorRepository: GlobalLowResolutionPositionRepository =
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
            )
        }

    internal fun getPlayerInfo(idx: Int): PlayerInfo? {
        return playerInfoRepository.wrapped.getOrNull(idx)
    }

    public fun alloc(idx: Int): PlayerInfo {
        return playerInfoRepository.wrapped.alloc(idx)
    }

    public fun prepare() {
        // Synchronize the known map sectors of everyone for this cycle
        for (i in 1..<capacity) {
            val info = playerInfoRepository.wrapped.getOrNull(i)
            if (info == null) {
                mapSectorRepository.markUnused(i)
            } else {
                mapSectorRepository.update(i, info.avatar.currentCoord)
            }
        }
        for (i in 1..<capacity) {
            playerInfoRepository.wrapped.getOrNull(i)?.prepareBitcodes(mapSectorRepository)
        }
    }

    internal fun getSector(idx: Int): LowResolutionPosition {
        return mapSectorRepository.getCurrentLowResolutionPosition(idx)
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
        mapSectorRepository.postUpdate()
    }
}
