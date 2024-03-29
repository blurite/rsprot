package net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo

import net.rsprot.protocol.internal.game.outgoing.info.TransientExtendedInfo
import net.rsprot.protocol.internal.game.outgoing.info.encoder.PrecomputedExtendedInfoEncoder
import net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.util.SpotAnim
import net.rsprot.protocol.shared.platform.PlatformType
import java.util.BitSet

@Suppress("MemberVisibilityCanBePrivate")
public class SpotAnimList(
    encoders: Array<PrecomputedExtendedInfoEncoder<SpotAnimList>?> = arrayOfNulls(PlatformType.COUNT),
) : TransientExtendedInfo<SpotAnimList, PrecomputedExtendedInfoEncoder<SpotAnimList>>(encoders) {
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
            spotanims[nextSetBit] = UNINITIALIZED_SPOTANIM
            nextSetBit = changelist.nextSetBit(nextSetBit + 1)
        } while (nextSetBit != -1)
        changelist.clear()
    }

    public companion object {
        private const val UNINITIALIZED_SPOTANIM = -1L
        private const val MAX_SPOTANIM_COUNT = 256
    }
}
