package net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo

import net.rsprot.protocol.internal.game.outgoing.info.TransientExtendedInfo
import net.rsprot.protocol.internal.game.outgoing.info.encoder.PrecomputedExtendedInfoEncoder
import net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.util.SpotAnim
import net.rsprot.protocol.internal.platform.PlatformMap
import java.util.BitSet

/**
 * The spotanim list is a specialized extended info block with compression logic built into it,
 * as the theoretical possibilities of this block are rather large.
 * This extended info block will track the modified slots using a bitset.
 * Instead of traversing the entire list at the end of a cycle to reset the properties,
 * it will follow the bitset's enabled bits to identify which slots to reset, if any.
 * As in most cases the answer is none - this should outperform array fills by quite a bit.
 * @param encoders the array of platform-specific encoders for spotanims.
 */
public class SpotAnimList(
    override val encoders: PlatformMap<PrecomputedExtendedInfoEncoder<SpotAnimList>>,
) : TransientExtendedInfo<SpotAnimList, PrecomputedExtendedInfoEncoder<SpotAnimList>>() {
    /**
     * The changelist that tracks all the slots which have been flagged for a spotanim update.
     */
    public val changelist: BitSet = BitSet(MAX_SPOTANIM_COUNT)

    /**
     * The array of spotanims on this avatar.
     * This array utilizes the bitpacked representation of a [SpotAnim].
     */
    public val spotanims: LongArray =
        LongArray(MAX_SPOTANIM_COUNT) {
            UNINITIALIZED_SPOTANIM
        }

    /**
     * Sets the spotanim in slot [slot].
     * This function will also flag the given slot for a change.
     * @param slot the slot of the spotanim to set.
     * @param spotAnim the spotanim to set.
     */
    public fun set(
        slot: Int,
        spotAnim: SpotAnim,
    ) {
        spotanims[slot] = spotAnim.packed
        changelist.set(slot)
    }

    /**
     * Traverses the bit set to determine which spotanims to clear out.
     */
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
