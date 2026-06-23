package net.rsprot.protocol.game.outgoing.codec.npcinfo.extendedinfo

import io.netty.buffer.ByteBufAllocator
import net.rsprot.buffer.JagByteBuf
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprot.compression.provider.HuffmanCodecProvider
import net.rsprot.protocol.internal.game.outgoing.info.encoder.PrecomputedExtendedInfoEncoder
import net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.Freeze

public class NpcFreezeEncoder : PrecomputedExtendedInfoEncoder<Freeze> {
    override fun precompute(
        alloc: ByteBufAllocator,
        huffmanCodecProvider: HuffmanCodecProvider,
        extendedInfo: Freeze,
    ): JagByteBuf {
        val buffer =
            alloc
                .buffer(5, 5)
                .toJagByteBuf()
        buffer.p2(extendedInfo.delay.toInt())
        buffer.p2(extendedInfo.duration.toInt())
        buffer.p1Alt2(if (extendedInfo.cancelSequence) 1 else 0)
        return buffer
    }
}
