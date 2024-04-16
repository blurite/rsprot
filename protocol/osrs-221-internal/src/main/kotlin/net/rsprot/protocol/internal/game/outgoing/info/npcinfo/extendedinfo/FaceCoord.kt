package net.rsprot.protocol.internal.game.outgoing.info.npcinfo.extendedinfo

import io.netty.buffer.ByteBufAllocator
import net.rsprot.compression.HuffmanCodec
import net.rsprot.protocol.internal.game.outgoing.info.TransientExtendedInfo
import net.rsprot.protocol.internal.game.outgoing.info.encoder.PrecomputedExtendedInfoEncoder
import net.rsprot.protocol.shared.platform.PlatformType

public class FaceCoord(
    encoders: Array<PrecomputedExtendedInfoEncoder<FaceCoord>?> = arrayOfNulls(PlatformType.COUNT),
    private val allocator: ByteBufAllocator,
    private val huffmanCodec: HuffmanCodec,
) : TransientExtendedInfo<FaceCoord, PrecomputedExtendedInfoEncoder<FaceCoord>>(encoders) {
    public var instant: Boolean = false
    public var x: UShort = 0xFFFFu
    public var z: UShort = 0xFFFFu

    override fun precompute() {
        for (id in 0..<PlatformType.COUNT) {
            val encoder = encoders[id] ?: continue
            val encoded = encoder.precompute(allocator, huffmanCodec, this)
            setBuffer(id, encoded.buffer)
        }
    }

    override fun clear() {
        releaseBuffers()
        this.instant = false
        this.x = 0xFFFFu
        this.z = 0xFFFFu
    }
}
