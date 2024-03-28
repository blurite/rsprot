package net.rsprot.protocol.game.outgoing.info.playerinfo.util

import java.util.BitSet

internal class ObserverExtendedInfoFlags(capacity: Int) {
    private val observerExtendedInfoFlags: IntArray = IntArray(capacity)
    private val flaggedAvatars: BitSet = BitSet(capacity)

    fun reset() {
        var nextSetBit = flaggedAvatars.nextSetBit(0)
        if (nextSetBit == -1) {
            return
        }
        do {
            observerExtendedInfoFlags[nextSetBit] = 0
            nextSetBit = flaggedAvatars.nextSetBit(nextSetBit + 1)
        } while (nextSetBit != -1)
        flaggedAvatars.clear()
    }

    fun addFlag(
        index: Int,
        flag: Int,
    ) {
        observerExtendedInfoFlags[index] = observerExtendedInfoFlags[index] or flag
    }

    fun getFlag(index: Int): Int {
        return observerExtendedInfoFlags[index]
    }
}
