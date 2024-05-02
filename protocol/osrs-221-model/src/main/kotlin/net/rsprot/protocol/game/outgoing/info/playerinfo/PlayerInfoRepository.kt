package net.rsprot.protocol.game.outgoing.info.playerinfo

import net.rsprot.protocol.common.client.OldSchoolClientType
import net.rsprot.protocol.game.outgoing.info.InfoRepository

/**
 * An array implementation that utilizes a reference queue to re-use objects created in the past.
 * This is particularly useful when dealing with objects that consume a lot of memory,
 * pooling in such cases prevents time taken by allocations and garbage collection.
 *
 * This particular implementation uses soft references to keep track of the deallocated
 * objects. Soft references only release their object if the JVM is about to run out of
 * memory, as a last resort.
 *
 * @param allocator the function that yields new elements on-demand, if none
 * are available within the reference queue.
 */
@Suppress("unused")
internal class PlayerInfoRepository(
    allocator: (index: Int, oldSchoolClientType: OldSchoolClientType) -> PlayerInfo,
) : InfoRepository<PlayerInfo>(allocator) {
    /**
     * The backing elements array used to store currently-in-use objects.
     */
    override val elements: Array<PlayerInfo?> = arrayOfNulls(PlayerInfoProtocol.PROTOCOL_CAPACITY)

    override fun informDeallocation(idx: Int) {
        for (element in elements) {
            if (element == null) {
                continue
            }
            element.avatar.extendedInfo.onOtherAvatarDeallocated(idx)
        }
    }

    override fun onDealloc(element: PlayerInfo) {
        element.onDealloc()
    }

    override fun onAlloc(
        element: PlayerInfo,
        idx: Int,
        oldSchoolClientType: OldSchoolClientType,
    ) {
        element.onAlloc(idx, oldSchoolClientType)
    }
}
