package net.rsprot.protocol.game.outgoing.codec.npcinfo.extendedinfo

import io.netty.buffer.ByteBufAllocator
import net.rsprot.buffer.JagByteBuf
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprot.compression.HuffmanCodec
import net.rsprot.protocol.common.game.outgoing.info.encoder.PrecomputedExtendedInfoEncoder
import net.rsprot.protocol.common.game.outgoing.info.npcinfo.extendedinfo.NpcTinting

public class NpcTintingEncoder : PrecomputedExtendedInfoEncoder<NpcTinting> {
    override fun precompute(
        alloc: ByteBufAllocator,
        huffmanCodec: HuffmanCodec,
        extendedInfo: NpcTinting,
    ): JagByteBuf {
        val buffer =
            alloc
                .buffer(10, 10)
                .toJagByteBuf()
        val tinting = extendedInfo.global
        buffer.p2(tinting.start.toInt())
        buffer.p2Alt3(tinting.end.toInt())
        buffer.p1Alt1(tinting.hue.toInt())
        buffer.p1(tinting.saturation.toInt())
        buffer.p1Alt3(tinting.lightness.toInt())
        buffer.p1Alt1(tinting.weight.toInt())
        return buffer
    }
}
