package net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo

import io.netty.buffer.ByteBufAllocator
import net.rsprot.compression.HuffmanCodec
import net.rsprot.protocol.internal.game.outgoing.info.TransientExtendedInfo
import net.rsprot.protocol.internal.game.outgoing.info.encoder.PrecomputedExtendedInfoEncoder
import net.rsprot.protocol.shared.platform.PlatformType

/**
 * The extended info block to make avatars face-lock onto another avatar, be it a NPC or a player.
 * @param encoders the array of platform-specific encoders for face pathing entity.
 * @param allocator the byte buffer allocator, used to pre-computation purposes.
 * @param huffmanCodec the huffman codec responsible for compressing public chat extended info block.
 */
public class FacePathingEntity(
    encoders: Array<PrecomputedExtendedInfoEncoder<FacePathingEntity>?> = arrayOfNulls(PlatformType.COUNT),
    private val allocator: ByteBufAllocator,
    private val huffmanCodec: HuffmanCodec,
) : TransientExtendedInfo<FacePathingEntity, PrecomputedExtendedInfoEncoder<FacePathingEntity>>(encoders) {
    /**
     * The index of the avatar to face-lock onto. For player avatars,
     * a value of 0x10000 is added onto the index to differentiate it.
     */
    public var index: Int = DEFAULT_VALUE

    override fun precompute() {
        for (id in 0..<PlatformType.COUNT) {
            val encoder = encoders[id] ?: continue
            val encoded = encoder.precompute(allocator, huffmanCodec, this)
            setBuffer(id, encoded.buffer)
        }
    }

    override fun clear() {
        releaseBuffers()
        index = DEFAULT_VALUE
    }

    public companion object {
        public const val DEFAULT_VALUE: Int = -1
    }
}
