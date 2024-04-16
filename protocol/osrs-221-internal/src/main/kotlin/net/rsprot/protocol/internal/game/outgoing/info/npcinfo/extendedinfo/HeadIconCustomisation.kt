package net.rsprot.protocol.internal.game.outgoing.info.npcinfo.extendedinfo

import io.netty.buffer.ByteBufAllocator
import net.rsprot.compression.HuffmanCodec
import net.rsprot.protocol.internal.game.outgoing.info.TransientExtendedInfo
import net.rsprot.protocol.internal.game.outgoing.info.encoder.PrecomputedExtendedInfoEncoder
import net.rsprot.protocol.shared.platform.PlatformType

public class HeadIconCustomisation(
    encoders: Array<PrecomputedExtendedInfoEncoder<HeadIconCustomisation>?> = arrayOfNulls(PlatformType.COUNT),
    private val allocator: ByteBufAllocator,
    private val huffmanCodec: HuffmanCodec,
) : TransientExtendedInfo<HeadIconCustomisation, PrecomputedExtendedInfoEncoder<HeadIconCustomisation>>(encoders) {
    public var flag: Int = 0
    public val headIconGroups: IntArray = IntArray(8)
    public val headIconIndices: ShortArray = ShortArray(8)

    override fun precompute() {
        for (id in 0..<PlatformType.COUNT) {
            val encoder = encoders[id] ?: continue
            val encoded = encoder.precompute(allocator, huffmanCodec, this)
            setBuffer(id, encoded.buffer)
        }
    }

    override fun clear() {
        releaseBuffers()
        flag = 0
        headIconGroups.fill(0)
        headIconIndices.fill(0)
    }
}
