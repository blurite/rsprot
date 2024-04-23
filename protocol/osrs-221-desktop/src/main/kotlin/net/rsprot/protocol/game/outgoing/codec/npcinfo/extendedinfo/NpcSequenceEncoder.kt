package net.rsprot.protocol.game.outgoing.codec.npcinfo.extendedinfo

import io.netty.buffer.ByteBufAllocator
import net.rsprot.buffer.JagByteBuf
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprot.compression.HuffmanCodec
import net.rsprot.protocol.common.game.outgoing.info.encoder.PrecomputedExtendedInfoEncoder
import net.rsprot.protocol.common.game.outgoing.info.shared.extendedinfo.Sequence

public class NpcSequenceEncoder : PrecomputedExtendedInfoEncoder<Sequence> {
    override fun precompute(
        alloc: ByteBufAllocator,
        huffmanCodec: HuffmanCodec,
        extendedInfo: Sequence,
    ): JagByteBuf {
        val buffer =
            alloc
                .buffer(3, 3)
                .toJagByteBuf()
        buffer.p2Alt3(extendedInfo.id.toInt())
        buffer.p1(extendedInfo.delay.toInt())
        return buffer
    }
}
