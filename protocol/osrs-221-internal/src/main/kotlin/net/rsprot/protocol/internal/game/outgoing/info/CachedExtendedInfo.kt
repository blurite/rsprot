package net.rsprot.protocol.internal.game.outgoing.info

public abstract class CachedExtendedInfo(capacity: Int) : ExtendedInfo() {
    public val otherChangesCounter: IntArray = IntArray(capacity)
    public var changeCounter: Int = 0

    protected fun resetCache() {
        otherChangesCounter.fill(0)
        changeCounter = 0
    }
}
