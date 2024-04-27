package net.rsprot.protocol.api

import net.rsprot.protocol.ProtCategory

public interface GameMessageCounter {
    public fun increment(protCategory: ProtCategory)

    public fun isFull(): Boolean

    public fun reset()
}
