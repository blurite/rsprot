package net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo

import io.netty.buffer.ByteBufAllocator
import net.rsprot.compression.HuffmanCodec
import net.rsprot.protocol.internal.game.outgoing.info.TransientExtendedInfo
import net.rsprot.protocol.internal.game.outgoing.info.encoder.PrecomputedExtendedInfoEncoder
import net.rsprot.protocol.shared.platform.PlatformType

public class Sequence(
    encoders: Array<PrecomputedExtendedInfoEncoder<Sequence>?> = arrayOfNulls(PlatformType.COUNT),
    private val allocator: ByteBufAllocator,
    private val huffmanCodec: HuffmanCodec,
) : TransientExtendedInfo<Sequence, PrecomputedExtendedInfoEncoder<Sequence>>(encoders) {
    public var id: UShort = 0xFFFFu
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
