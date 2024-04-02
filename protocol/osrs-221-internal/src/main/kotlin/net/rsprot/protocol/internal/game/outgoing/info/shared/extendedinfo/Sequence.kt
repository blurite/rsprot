package net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo

import io.netty.buffer.ByteBufAllocator
import net.rsprot.compression.HuffmanCodec
import net.rsprot.protocol.internal.game.outgoing.info.TransientExtendedInfo
import net.rsprot.protocol.internal.game.outgoing.info.encoder.PrecomputedExtendedInfoEncoder
import net.rsprot.protocol.shared.platform.PlatformType

/**
 * The sequence mask defines what animation an avatar is playing.
 * @param encoders the array of platform-specific encoders for sequence.
 * @param allocator the byte buffer allocator, used to pre-computation purposes.
 * @param huffmanCodec the huffman codec responsible for compressing public chat extended info block.
 */
public class Sequence(
    encoders: Array<PrecomputedExtendedInfoEncoder<Sequence>?> = arrayOfNulls(PlatformType.COUNT),
    private val allocator: ByteBufAllocator,
    private val huffmanCodec: HuffmanCodec,
) : TransientExtendedInfo<Sequence, PrecomputedExtendedInfoEncoder<Sequence>>(encoders) {
    /**
     * The id of the animation to play.
     */
    public var id: UShort = 0xFFFFu

    /**
     * The delay in client cycles (20ms/cc) until the given animation begins playing.
     */
    public var delay: UShort = 0u

    override fun precompute() {
        for (id in 0..<PlatformType.COUNT) {
            val encoder = encoders[id] ?: continue
            val encoded = encoder.precompute(allocator, huffmanCodec, this)
            setBuffer(id, encoded.buffer)
        }
    }

    override fun clear() {
        releaseBuffers()
        id = 0xFFFFu
        delay = 0u
    }
}
