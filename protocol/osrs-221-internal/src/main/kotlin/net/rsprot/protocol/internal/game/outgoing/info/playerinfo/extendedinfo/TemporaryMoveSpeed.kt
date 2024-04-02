package net.rsprot.protocol.internal.game.outgoing.info.playerinfo.extendedinfo

import io.netty.buffer.ByteBufAllocator
import net.rsprot.compression.HuffmanCodec
import net.rsprot.protocol.internal.game.outgoing.info.TransientExtendedInfo
import net.rsprot.protocol.internal.game.outgoing.info.encoder.PrecomputedExtendedInfoEncoder
import net.rsprot.protocol.shared.platform.PlatformType

/**
 * The temporary move speed is used to set a move speed for a single cycle, commonly done
 * when the player has run enabled through the orb, but decides to only walk a single tile instead.
 * Rather than to switch the main mode over to walking, it utilizes the temporary move speed
 * so the primary one will remain as running after this one cycle, as they are far more likely
 * to utilize the move speed described by their run orb.
 * @param encoders the array of platform-specific encoders for temporary move speed.
 * @param allocator the byte buffer allocator, used to pre-computation purposes.
 * @param huffmanCodec the huffman codec responsible for compressing public chat extended info block.
 */
public class TemporaryMoveSpeed(
    encoders: Array<PrecomputedExtendedInfoEncoder<TemporaryMoveSpeed>?> = arrayOfNulls(PlatformType.COUNT),
    private val allocator: ByteBufAllocator,
    private val huffmanCodec: HuffmanCodec,
) : TransientExtendedInfo<TemporaryMoveSpeed, PrecomputedExtendedInfoEncoder<TemporaryMoveSpeed>>(encoders) {
    /**
     * The movement speed of this avatar for a single cycle.
     */
    public var value: Int = -1

    override fun precompute() {
        for (id in 0..<PlatformType.COUNT) {
            val encoder = encoders[id] ?: continue
            val encoded = encoder.precompute(allocator, huffmanCodec, this)
            setBuffer(id, encoded.buffer)
        }
    }

    override fun clear() {
        releaseBuffers()
        value = -1
    }
}
