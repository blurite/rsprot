package net.rsprot.protocol.game.outgoing.info.util

import net.rsprot.protocol.shared.platform.PlatformType

public interface ReferencePooledObject {
    public fun onAlloc(
        index: Int,
        platformType: PlatformType,
    )

    public fun onDealloc()
}
