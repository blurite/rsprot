package net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo

import io.netty.buffer.ByteBufAllocator
import net.rsprot.compression.HuffmanCodec
import net.rsprot.protocol.internal.game.outgoing.info.TransientExtendedInfo
import net.rsprot.protocol.internal.game.outgoing.info.encoder.PrecomputedExtendedInfoEncoder
import net.rsprot.protocol.shared.platform.PlatformType

public class FacePathingEntity(
    encoders: Array<PrecomputedExtendedInfoEncoder<FacePathingEntity>?> = arrayOfNulls(PlatformType.COUNT),
    private val allocator: ByteBufAllocator,
    private val huffmanCodec: HuffmanCodec,
) : TransientExtendedInfo<FacePathingEntity, PrecomputedExtendedInfoEncoder<FacePathingEntity>>(encoders) {
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
