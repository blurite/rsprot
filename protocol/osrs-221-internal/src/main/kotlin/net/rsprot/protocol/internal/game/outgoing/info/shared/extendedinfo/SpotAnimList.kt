package net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo

import io.netty.buffer.ByteBufAllocator
import net.rsprot.compression.HuffmanCodec
import net.rsprot.protocol.internal.game.outgoing.info.TransientExtendedInfo
import net.rsprot.protocol.internal.game.outgoing.info.encoder.PrecomputedExtendedInfoEncoder
import net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.util.SpotAnim
import net.rsprot.protocol.shared.platform.PlatformType
import java.util.BitSet

@Suppress("MemberVisibilityCanBePrivate")
public class SpotAnimList(
    encoders: Array<PrecomputedExtendedInfoEncoder<SpotAnimList>?> = arrayOfNulls(PlatformType.COUNT),
    private val allocator: ByteBufAllocator,
    private val huffmanCodec: HuffmanCodec,
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

    override fun precompute() {
        for (id in 0..<PlatformType.COUNT) {
            val encoder = encoders[id] ?: continue
            val encoded = encoder.precompute(allocator, huffmanCodec, this)
            setBuffer(id, encoded.buffer)
        }
    }

    public companion object {
        private const val UNINITIALIZED_SPOTANIM = -1L
        private const val MAX_SPOTANIM_COUNT = 256
    }
}
