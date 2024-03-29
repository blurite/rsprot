package net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo

import io.netty.buffer.ByteBufAllocator
import net.rsprot.compression.HuffmanCodec
import net.rsprot.protocol.internal.game.outgoing.info.TransientExtendedInfo
import net.rsprot.protocol.internal.game.outgoing.info.encoder.PrecomputedExtendedInfoEncoder
import net.rsprot.protocol.shared.platform.PlatformType

public class ExactMove(
    encoders: Array<PrecomputedExtendedInfoEncoder<ExactMove>?> = arrayOfNulls(PlatformType.COUNT),
    private val allocator: ByteBufAllocator,
    private val huffmanCodec: HuffmanCodec,
) : TransientExtendedInfo<ExactMove, PrecomputedExtendedInfoEncoder<ExactMove>>(encoders) {
    public var deltaX1: UByte = 0u
    public var deltaZ1: UByte = 0u
    public var delay1: UShort = 0u
    public var deltaX2: UByte = 0u
    public var deltaZ2: UByte = 0u
    public var delay2: UShort = 0u
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
