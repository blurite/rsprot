package net.rsprot.protocol.game.outgoing.info.worldentityinfo

import net.rsprot.protocol.common.client.OldSchoolClientType
import net.rsprot.protocol.game.outgoing.info.InfoRepository

internal class WorldEntityRepository(
    allocator: (index: Int, oldSchoolClientType: OldSchoolClientType) -> WorldEntityInfo,
) : InfoRepository<WorldEntityInfo>(allocator) {
    override val elements: Array<WorldEntityInfo?> = arrayOfNulls(WorldEntityProtocol.CAPACITY)

    override fun informDeallocation(idx: Int) {
        TODO("Not yet implemented")
    }

    override fun onDealloc(element: WorldEntityInfo) {
        TODO("Not yet implemented")
    }

    override fun onAlloc(
        element: WorldEntityInfo,
        idx: Int,
        oldSchoolClientType: OldSchoolClientType,
    ) {
        TODO("Not yet implemented")
    }
}
