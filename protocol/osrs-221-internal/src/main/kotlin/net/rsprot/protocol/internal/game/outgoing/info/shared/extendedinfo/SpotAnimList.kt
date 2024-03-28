package net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo

import net.rsprot.protocol.internal.game.outgoing.info.TransientExtendedInfo
import net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.util.SpotAnim
import java.util.BitSet

@Suppress("MemberVisibilityCanBePrivate")
public class SpotAnimList : TransientExtendedInfo() {
    public val changelist: BitSet = BitSet(MAX_SPOTANIM_COUNT)
    public val spotanims: LongArray =
        LongArray(MAX_SPOTANIM_COUNT) {
            UNINITIALIZED_SPOTANIM
        }

    public fun set(
        slot: Int,
        spotAnim: SpotAnim,
    ) {
        spotanims[slot] = spotAnim.packed
        changelist.set(slot)
    }

    override fun clear() {
        releaseBuffers()
        var nextSetBit = changelist.nextSetBit(0)
        if (nextSetBit == -1) {
            return
        }
        do {
            spotanims[nextSetBit ushr SPOTANIM_SHIFT] = UNINITIALIZED_SPOTANIM
            nextSetBit = changelist.nextSetBit(nextSetBit + 1)
        } while (nextSetBit != -1)
        changelist.clear()
    }

    public companion object {
        private const val UNINITIALIZED_SPOTANIM = -1L
        private const val MAX_SPOTANIM_COUNT = 256
        private const val SPOTANIM_SHIFT = MAX_SPOTANIM_COUNT / Long.SIZE_BITS
    }
}
