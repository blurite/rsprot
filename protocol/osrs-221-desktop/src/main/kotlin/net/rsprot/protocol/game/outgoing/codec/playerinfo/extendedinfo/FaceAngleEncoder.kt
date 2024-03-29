package net.rsprot.protocol.game.outgoing.codec.playerinfo.extendedinfo

import io.netty.buffer.ByteBufAllocator
import net.rsprot.buffer.JagByteBuf
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprot.compression.HuffmanCodec
import net.rsprot.protocol.game.outgoing.info.extendedinfo.PrecomputedExtendedInfoEncoder
import net.rsprot.protocol.internal.game.outgoing.info.playerinfo.extendedinfo.FaceAngle

public class FaceAngleEncoder : PrecomputedExtendedInfoEncoder<FaceAngle> {
    override fun precompute(
        alloc: ByteBufAllocator,
        huffmanCodec: HuffmanCodec,
        extendedInfo: FaceAngle,
    ): JagByteBuf {
        val buffer =
            alloc
                .buffer(2, 2)
                .toJagByteBuf()
        buffer.p2(extendedInfo.angle)
        return buffer
    }
}
