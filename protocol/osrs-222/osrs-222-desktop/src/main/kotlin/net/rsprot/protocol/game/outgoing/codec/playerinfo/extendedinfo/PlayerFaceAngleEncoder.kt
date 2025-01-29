package net.rsprot.protocol.game.outgoing.codec.playerinfo.extendedinfo

import io.netty.buffer.ByteBufAllocator
import net.rsprot.buffer.JagByteBuf
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprot.compression.provider.HuffmanCodecProvider
import net.rsprot.protocol.internal.game.outgoing.info.encoder.PrecomputedExtendedInfoEncoder
import net.rsprot.protocol.internal.game.outgoing.info.playerinfo.extendedinfo.FaceAngle

public class PlayerFaceAngleEncoder : PrecomputedExtendedInfoEncoder<FaceAngle> {
    override fun precompute(
        alloc: ByteBufAllocator,
        huffmanCodecProvider: HuffmanCodecProvider,
        extendedInfo: FaceAngle,
    ): JagByteBuf {
        val buffer =
            alloc
                .buffer(2, 2)
                .toJagByteBuf()
        buffer.p2Alt1(extendedInfo.angle.toInt())
        return buffer
    }
}
