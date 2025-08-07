package net.rsprot.protocol.game.outgoing.info.worldentityinfo

import net.rsprot.protocol.common.client.OldSchoolClientType
import net.rsprot.protocol.game.outgoing.info.InfoRepository

/**
 * A repository for world entity info instances.
 * @param allocator the allocator used to return a new or re-used world entity info
 * instance, based on the provided player's index and client type.
 * @property elements the array of currently in used world entity info objects.
 */
internal class WorldEntityInfoRepository(
    allocator: (index: Int, oldSchoolClientType: OldSchoolClientType) -> WorldEntityInfo,
) : InfoRepository<WorldEntityInfo>(allocator) {
    override val elements: Array<WorldEntityInfo?> = arrayOfNulls(WorldEntityProtocol.CAPACITY)

    override fun informDeallocation(idx: Int) {
        // No-op
    }

    override fun onDealloc(element: WorldEntityInfo) {
        element.onDealloc()
    }

    override fun onAlloc(
        element: WorldEntityInfo,
        idx: Int,
        oldSchoolClientType: OldSchoolClientType,
        newInstance: Boolean,
    ) {
        element.onAlloc(idx, oldSchoolClientType, newInstance)
    }
}
