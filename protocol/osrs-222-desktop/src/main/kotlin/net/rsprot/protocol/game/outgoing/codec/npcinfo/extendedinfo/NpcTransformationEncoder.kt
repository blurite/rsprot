package net.rsprot.protocol.game.outgoing.codec.npcinfo.extendedinfo

import io.netty.buffer.ByteBufAllocator
import net.rsprot.buffer.JagByteBuf
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprot.compression.provider.HuffmanCodecProvider
import net.rsprot.protocol.common.game.outgoing.info.encoder.PrecomputedExtendedInfoEncoder
import net.rsprot.protocol.common.game.outgoing.info.npcinfo.extendedinfo.Transformation

public class NpcTransformationEncoder : PrecomputedExtendedInfoEncoder<Transformation> {
    override fun precompute(
        alloc: ByteBufAllocator,
        huffmanCodecProvider: HuffmanCodecProvider,
        extendedInfo: Transformation,
    ): JagByteBuf {
        val buffer =
            alloc
                .buffer(2, 2)
                .toJagByteBuf()
        buffer.p2Alt3(extendedInfo.id.toInt())
        return buffer
    }
}
