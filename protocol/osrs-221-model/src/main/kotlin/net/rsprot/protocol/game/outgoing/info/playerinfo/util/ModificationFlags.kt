package net.rsprot.protocol.game.outgoing.info.playerinfo.util

internal class ModificationFlags {
    private var previousCycleFlags: LongArray = LongArray(CAPACITY_IN_LONGS)
    private var currentCycleFlags: LongArray = LongArray(CAPACITY_IN_LONGS)

    fun flip() {
        val previous = previousCycleFlags
        val current = currentCycleFlags
        this.previousCycleFlags = current
        this.currentCycleFlags = previous
        previous.fill(0L)
    }

    fun isUnmodified(index: Int): Boolean {
        val slot = index ushr LOG_BITS_PER_LONG
        val flag = 1L shl (index and MASK_BITS_PER_LONG)
        return previousCycleFlags[slot] and flag != 0L
    }

    fun markUnmodified(index: Int) {
        val slot = index ushr LOG_BITS_PER_LONG
        val flag = 1L shl (index and MASK_BITS_PER_LONG)
        currentCycleFlags[slot] = currentCycleFlags[slot] or flag
    }

    private companion object {
        private const val CAPACITY: Int = 2048
        private const val LOG_BITS_PER_LONG = 6
        private const val CAPACITY_IN_LONGS: Int = CAPACITY ushr LOG_BITS_PER_LONG
        private const val LONG_SIZE_BITS = 1 shl LOG_BITS_PER_LONG
        private const val MASK_BITS_PER_LONG = LONG_SIZE_BITS - 1
    }
}
