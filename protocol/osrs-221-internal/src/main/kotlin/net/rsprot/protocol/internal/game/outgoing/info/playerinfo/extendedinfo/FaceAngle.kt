package net.rsprot.protocol.internal.game.outgoing.info.playerinfo.extendedinfo

import io.netty.buffer.ByteBufAllocator
import net.rsprot.compression.HuffmanCodec
import net.rsprot.protocol.internal.game.outgoing.info.TransientExtendedInfo
import net.rsprot.protocol.internal.game.outgoing.info.encoder.PrecomputedExtendedInfoEncoder
import net.rsprot.protocol.shared.platform.PlatformType

/**
 * The extended info block responsible for making an avatar turn towards a specific
 * angle.
 * @param encoders the array of platform-specific encoders for face angle.
 * @param allocator the byte buffer allocator, used to pre-computation purposes.
 * @param huffmanCodec the huffman codec responsible for compressing public chat extended info block.
 */
public class FaceAngle(
    encoders: Array<PrecomputedExtendedInfoEncoder<FaceAngle>?> = arrayOfNulls(PlatformType.COUNT),
    private val allocator: ByteBufAllocator,
    private val huffmanCodec: HuffmanCodec,
) : TransientExtendedInfo<FaceAngle, PrecomputedExtendedInfoEncoder<FaceAngle>>(encoders) {
    /**
     * The value of the angle for this avatar to turn towards.
     */
    public var angle: UShort = UShort.MAX_VALUE

    override fun precompute() {
        for (id in 0..<PlatformType.COUNT) {
            val encoder = encoders[id] ?: continue
            val encoded = encoder.precompute(allocator, huffmanCodec, this)
            setBuffer(id, encoded.buffer)
        }
    }

    override fun clear() {
        releaseBuffers()
        angle = UShort.MAX_VALUE
    }
}
