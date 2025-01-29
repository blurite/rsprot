package net.rsprot.protocol.game.outgoing.codec.playerinfo.extendedinfo

import io.netty.buffer.ByteBufAllocator
import net.rsprot.buffer.JagByteBuf
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprot.compression.provider.HuffmanCodecProvider
import net.rsprot.protocol.internal.game.outgoing.info.encoder.PrecomputedExtendedInfoEncoder
import net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.FacePathingEntity

public class PlayerFacePathingEntityEncoder : PrecomputedExtendedInfoEncoder<FacePathingEntity> {
    override fun precompute(
        alloc: ByteBufAllocator,
        huffmanCodecProvider: HuffmanCodecProvider,
        extendedInfo: FacePathingEntity,
    ): JagByteBuf {
        val buffer =
            alloc
                .buffer(3, 3)
                .toJagByteBuf()
        buffer.p2Alt1(extendedInfo.index)
        buffer.p1Alt3(extendedInfo.index shr 16)
        return buffer
    }
}
