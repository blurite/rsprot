package net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo

import io.netty.buffer.ByteBufAllocator
import net.rsprot.compression.HuffmanCodec
import net.rsprot.protocol.internal.game.outgoing.info.TransientExtendedInfo
import net.rsprot.protocol.internal.game.outgoing.info.encoder.PrecomputedExtendedInfoEncoder
import net.rsprot.protocol.shared.platform.PlatformType

/**
 * The exactmove extended info block is used to provide precise fine-tuned visual movement
 * of an avatar.
 * @param encoders the array of platform-specific encoders for exact move.
 * @param allocator the byte buffer allocator, used to pre-computation purposes.
 * @param huffmanCodec the huffman codec responsible for compressing public chat extended info block.
 */
public class ExactMove(
    encoders: Array<PrecomputedExtendedInfoEncoder<ExactMove>?> = arrayOfNulls(PlatformType.COUNT),
    private val allocator: ByteBufAllocator,
    private val huffmanCodec: HuffmanCodec,
) : TransientExtendedInfo<ExactMove, PrecomputedExtendedInfoEncoder<ExactMove>>(encoders) {
    /**
     * The coordinate delta between the current absolute
     * x coordinate and where the avatar is going.
     */
    public var deltaX1: UByte = 0u

    /**
     * The coordinate delta between the current absolute
     * z coordinate and where the avatar is going.
     */
    public var deltaZ1: UByte = 0u

    /**
     * Delay1 defines how many client cycles (20ms/cc) until the avatar arrives
     * at x/z 1 coordinate.
     */
    public var delay1: UShort = 0u

    /**
     * The coordinate delta between the current absolute
     * x coordinate and where the avatar is going.
     */
    public var deltaX2: UByte = 0u

    /**
     * The coordinate delta between the current absolute
     * z coordinate and where the avatar is going.
     */
    public var deltaZ2: UByte = 0u

    /**
     * Delay2 defines how many client cycles (20ms/cc) until the avatar arrives
     * at x/z 2 coordinate.
     */
    public var delay2: UShort = 0u

    /**
     * The angle the avatar will be facing throughout the exact movement,
     * with 0 implying south, 512 west, 1024 north and 1536 east; interpolate
     * between to get finer directions.
     */
    public var direction: UShort = 0u

    override fun precompute() {
        for (id in 0..<PlatformType.COUNT) {
            val encoder = encoders[id] ?: continue
            val encoded = encoder.precompute(allocator, huffmanCodec, this)
            setBuffer(id, encoded.buffer)
        }
    }

    override fun clear() {
        releaseBuffers()
        deltaX1 = 0u
        deltaZ1 = 0u
        delay1 = 0u
        deltaX2 = 0u
        deltaZ2 = 0u
        delay2 = 0u
        direction = 0u
    }
}
