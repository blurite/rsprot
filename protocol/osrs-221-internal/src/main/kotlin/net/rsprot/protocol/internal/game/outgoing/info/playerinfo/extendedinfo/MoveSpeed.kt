package net.rsprot.protocol.internal.game.outgoing.info.playerinfo.extendedinfo

import io.netty.buffer.ByteBufAllocator
import net.rsprot.compression.HuffmanCodec
import net.rsprot.protocol.internal.game.outgoing.info.TransientExtendedInfo
import net.rsprot.protocol.internal.game.outgoing.info.encoder.PrecomputedExtendedInfoEncoder
import net.rsprot.protocol.shared.platform.PlatformType

/**
 * The movement speed extended info block.
 * Unlike most extended info blocks, the [value] will last as long as the server tells it to.
 * The client will also temporarily cache it for the duration that it sees an avatar in high resolution.
 * Whenever an avatar moves, unless the move speed has been overwritten, this is the speed
 * that it will use for the movement, barring any special mechanics.
 * If an avatar goes from high resolution to low resolution, the client **will not** cache this,
 * and a new status update must be written when the opposite transition occurs.
 * This move speed status should typically be synchronized with the state of the "Run orb".
 * @param encoders the array of platform-specific encoders for appearance.
 * @param allocator the byte buffer allocator, used to pre-computation purposes.
 * @param huffmanCodec the huffman codec responsible for compressing public chat extended info block.
 */
public class MoveSpeed(
    encoders: Array<PrecomputedExtendedInfoEncoder<MoveSpeed>?> = arrayOfNulls(PlatformType.COUNT),
    private val allocator: ByteBufAllocator,
    private val huffmanCodec: HuffmanCodec,
) : TransientExtendedInfo<MoveSpeed, PrecomputedExtendedInfoEncoder<MoveSpeed>>(encoders) {
    /**
     * The current movement speed of this avatar.
     */
    public var value: Int = DEFAULT_MOVESPEED

    override fun precompute() {
        for (id in 0..<PlatformType.COUNT) {
            val encoder = encoders[id] ?: continue
            val encoded = encoder.precompute(allocator, huffmanCodec, this)
            setBuffer(id, encoded.buffer)
        }
    }

    override fun clear() {
        releaseBuffers()
        value = DEFAULT_MOVESPEED
    }

    public companion object {
        public const val DEFAULT_MOVESPEED: Int = 0
    }
}
