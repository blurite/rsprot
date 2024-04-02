package net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo

import io.netty.buffer.ByteBufAllocator
import net.rsprot.compression.HuffmanCodec
import net.rsprot.protocol.internal.game.outgoing.info.TransientExtendedInfo
import net.rsprot.protocol.internal.game.outgoing.info.encoder.PrecomputedExtendedInfoEncoder
import net.rsprot.protocol.shared.platform.PlatformType

/**
 * The say extended info block tracks any overhead chat set by the server,
 * through content. Public chat will not utilize this.
 * @param encoders the array of platform-specific encoders for say.
 * @param allocator the byte buffer allocator, used to pre-computation purposes.
 * @param huffmanCodec the huffman codec responsible for compressing public chat extended info block.
 */
public class Say(
    encoders: Array<PrecomputedExtendedInfoEncoder<Say>?> = arrayOfNulls(PlatformType.COUNT),
    private val allocator: ByteBufAllocator,
    private val huffmanCodec: HuffmanCodec,
) : TransientExtendedInfo<Say, PrecomputedExtendedInfoEncoder<Say>>(encoders) {
    /**
     * The text to render over the avatar.
     */
    public var text: String? = null

    override fun precompute() {
        for (id in 0..<PlatformType.COUNT) {
            val encoder = encoders[id] ?: continue
            val encoded = encoder.precompute(allocator, huffmanCodec, this)
            setBuffer(id, encoded.buffer)
        }
    }

    override fun clear() {
        releaseBuffers()
        text = null
    }
}
