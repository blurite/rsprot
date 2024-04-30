package net.rsprot.protocol.api

import net.rsprot.protocol.ClientProtCategory

public interface GameMessageCounter {
    public fun increment(clientProtCategory: ClientProtCategory)

    public fun isFull(): Boolean

    public fun reset()
}
