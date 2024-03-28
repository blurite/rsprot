package net.rsprot.protocol.game.outgoing.info.playerinfo

import net.rsprot.protocol.game.outgoing.info.util.SoftReferencePooledObjectArray

@JvmInline
public value class PlayerInfoRepository internal constructor(
    @PublishedApi internal val wrapped: SoftReferencePooledObjectArray<PlayerInfo>,
) {
    public constructor(
        capacity: Int,
        allocator: (index: Int) -> PlayerInfo,
    ) : this(
        SoftReferencePooledObjectArray(capacity, allocator),
    )
}
