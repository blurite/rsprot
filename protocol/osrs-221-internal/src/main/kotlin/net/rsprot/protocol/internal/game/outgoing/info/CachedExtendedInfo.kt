package net.rsprot.protocol.internal.game.outgoing.info

import net.rsprot.protocol.internal.game.outgoing.info.encoder.ExtendedInfoEncoder

public abstract class CachedExtendedInfo<in T : ExtendedInfo<T, E>, E : ExtendedInfoEncoder<T>>(
    capacity: Int,
    encoders: Array<E?>,
) : ExtendedInfo<T, E>(encoders) {
    public val otherChangesCounter: IntArray = IntArray(capacity)
    public var changeCounter: Int = 0

    protected fun resetCache() {
        otherChangesCounter.fill(0)
        changeCounter = 0
    }
}
