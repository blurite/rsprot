package net.rsprot.protocol.game.outgoing.info.worldentityinfo

import net.rsprot.protocol.common.client.OldSchoolClientType
import net.rsprot.protocol.game.outgoing.info.InfoRepository

internal class WorldEntityRepository(
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
    ) {
        element.onAlloc(idx, oldSchoolClientType)
    }
}
