package net.rsprot.protocol.game.outgoing.codec.npcinfo.extendedinfo

import io.netty.buffer.ByteBufAllocator
import net.rsprot.buffer.JagByteBuf
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprot.compression.provider.HuffmanCodecProvider
import net.rsprot.protocol.internal.game.outgoing.info.encoder.PrecomputedExtendedInfoEncoder
import net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.FaceAngle

public class NpcFaceAngleEncoder : PrecomputedExtendedInfoEncoder<FaceAngle> {
    override fun precompute(
        alloc: ByteBufAllocator,
        huffmanCodecProvider: HuffmanCodecProvider,
        extendedInfo: FaceAngle,
    ): JagByteBuf {
        val buffer =
            alloc
                .buffer(3, 3)
                .toJagByteBuf()
        buffer.p2Alt1(extendedInfo.angle.toInt())
        buffer.p1Alt1(if (extendedInfo.instant) 1 else 0)
        return buffer
    }
}
