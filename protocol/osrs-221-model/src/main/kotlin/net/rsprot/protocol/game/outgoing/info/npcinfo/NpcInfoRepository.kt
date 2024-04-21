package net.rsprot.protocol.game.outgoing.info.npcinfo

import net.rsprot.protocol.game.outgoing.info.InfoRepository
import net.rsprot.protocol.shared.platform.PlatformType

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
@ExperimentalUnsignedTypes
internal class NpcInfoRepository(
    allocator: (index: Int, platformType: PlatformType) -> NpcInfo,
) : InfoRepository<NpcInfo>(allocator) {
    override val elements: Array<NpcInfo?> = arrayOfNulls(NpcInfoProtocol.PROTOCOL_CAPACITY)

    override fun informDeallocation(idx: Int) {
        // No-op
    }

    override fun onDealloc(element: NpcInfo) {
        element.onDealloc()
    }

    override fun onAlloc(
        element: NpcInfo,
        idx: Int,
        platformType: PlatformType,
    ) {
        element.onAlloc(idx, platformType)
    }
}
