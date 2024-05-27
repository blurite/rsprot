package net.rsprot.protocol.game.outgoing.codec.npcinfo.extendedinfo

import io.netty.buffer.ByteBufAllocator
import net.rsprot.buffer.JagByteBuf
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprot.compression.provider.HuffmanCodecProvider
import net.rsprot.protocol.common.game.outgoing.info.encoder.PrecomputedExtendedInfoEncoder
import net.rsprot.protocol.common.game.outgoing.info.npcinfo.extendedinfo.HeadIconCustomisation

public class NpcHeadIconCustomisationEncoder : PrecomputedExtendedInfoEncoder<HeadIconCustomisation> {
    override fun precompute(
        alloc: ByteBufAllocator,
        huffmanCodecProvider: HuffmanCodecProvider,
        extendedInfo: HeadIconCustomisation,
    ): JagByteBuf {
        val capacity = 1 + 8 * 6
        val buffer =
            alloc
                .buffer(capacity, capacity)
                .toJagByteBuf()
        val flag = extendedInfo.flag
        buffer.p1(flag)
        for (i in extendedInfo.headIconGroups.indices) {
            if (flag and (1 shl i) == 0) {
                continue
            }
            val group = extendedInfo.headIconGroups[i]
            val index = extendedInfo.headIconIndices[i].toInt()
            buffer.pSmart2or4null(group)
            buffer.pSmart1or2null(index)
        }
        return buffer
    }
}
