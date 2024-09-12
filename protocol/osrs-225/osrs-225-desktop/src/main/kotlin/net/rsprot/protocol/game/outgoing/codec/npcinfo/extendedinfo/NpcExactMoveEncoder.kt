package net.rsprot.protocol.game.outgoing.codec.npcinfo.extendedinfo

import io.netty.buffer.ByteBufAllocator
import net.rsprot.buffer.JagByteBuf
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprot.compression.provider.HuffmanCodecProvider
import net.rsprot.protocol.common.game.outgoing.info.encoder.PrecomputedExtendedInfoEncoder
import net.rsprot.protocol.common.game.outgoing.info.shared.extendedinfo.ExactMove

public class NpcExactMoveEncoder : PrecomputedExtendedInfoEncoder<ExactMove> {
    override fun precompute(
        alloc: ByteBufAllocator,
        huffmanCodecProvider: HuffmanCodecProvider,
        extendedInfo: ExactMove,
    ): JagByteBuf {
        val buffer =
            alloc
                .buffer(10, 10)
                .toJagByteBuf()
        buffer.p1Alt3(extendedInfo.deltaX1.toInt())
        buffer.p1Alt1(extendedInfo.deltaZ1.toInt())
        buffer.p1(extendedInfo.deltaX2.toInt())
        buffer.p1Alt2(extendedInfo.deltaZ2.toInt())
        buffer.p2Alt1(extendedInfo.delay1.toInt())
        buffer.p2Alt1(extendedInfo.delay2.toInt())
        buffer.p2(extendedInfo.direction.toInt())
        return buffer
    }
}
