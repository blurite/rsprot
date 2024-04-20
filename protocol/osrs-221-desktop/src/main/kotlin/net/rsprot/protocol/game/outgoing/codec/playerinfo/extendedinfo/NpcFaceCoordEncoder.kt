package net.rsprot.protocol.game.outgoing.codec.playerinfo.extendedinfo

import io.netty.buffer.ByteBufAllocator
import net.rsprot.buffer.JagByteBuf
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprot.compression.HuffmanCodec
import net.rsprot.protocol.internal.game.outgoing.info.encoder.PrecomputedExtendedInfoEncoder
import net.rsprot.protocol.internal.game.outgoing.info.npcinfo.extendedinfo.FaceCoord

public class NpcFaceCoordEncoder : PrecomputedExtendedInfoEncoder<FaceCoord> {
    override fun precompute(
        alloc: ByteBufAllocator,
        huffmanCodec: HuffmanCodec,
        extendedInfo: FaceCoord,
    ): JagByteBuf {
        val buffer =
            alloc
                .buffer(5, 5)
                .toJagByteBuf()
        buffer.p2Alt3(extendedInfo.x.toInt())
        buffer.p2Alt3(extendedInfo.z.toInt())
        buffer.p1(if (extendedInfo.instant) 1 else 0)
        return buffer
    }
}
