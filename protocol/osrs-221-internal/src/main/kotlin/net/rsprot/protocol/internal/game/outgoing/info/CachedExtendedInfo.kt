package net.rsprot.protocol.internal.game.outgoing.info

public abstract class CachedExtendedInfo(capacity: Int) : ExtendedInfo() {
    public val cache: IntArray = IntArray(capacity)

    protected fun resetCache() {
        cache.fill(0)
    }
}
