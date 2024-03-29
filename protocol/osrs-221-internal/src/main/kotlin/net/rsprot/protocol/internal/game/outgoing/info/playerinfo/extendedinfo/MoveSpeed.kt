package net.rsprot.protocol.internal.game.outgoing.info.playerinfo.extendedinfo

import io.netty.buffer.ByteBufAllocator
import net.rsprot.compression.HuffmanCodec
import net.rsprot.protocol.internal.game.outgoing.info.TransientExtendedInfo
import net.rsprot.protocol.internal.game.outgoing.info.encoder.PrecomputedExtendedInfoEncoder
import net.rsprot.protocol.shared.platform.PlatformType

public class MoveSpeed(
    encoders: Array<PrecomputedExtendedInfoEncoder<MoveSpeed>?> = arrayOfNulls(PlatformType.COUNT),
    private val allocator: ByteBufAllocator,
    private val huffmanCodec: HuffmanCodec,
) : TransientExtendedInfo<MoveSpeed, PrecomputedExtendedInfoEncoder<MoveSpeed>>(encoders) {
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
