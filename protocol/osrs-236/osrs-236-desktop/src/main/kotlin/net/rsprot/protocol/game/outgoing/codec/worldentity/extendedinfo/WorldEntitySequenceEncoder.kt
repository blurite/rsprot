package net.rsprot.protocol.game.outgoing.codec.worldentity.extendedinfo

import io.netty.buffer.ByteBufAllocator
import net.rsprot.buffer.JagByteBuf
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprot.compression.provider.HuffmanCodecProvider
import net.rsprot.protocol.internal.game.outgoing.info.encoder.PrecomputedExtendedInfoEncoder
import net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.Sequence

public class WorldEntitySequenceEncoder : PrecomputedExtendedInfoEncoder<Sequence> {
    override fun precompute(
        alloc: ByteBufAllocator,
        huffmanCodecProvider: HuffmanCodecProvider,
        extendedInfo: Sequence,
    ): JagByteBuf {
        val buffer =
            alloc
                .buffer(3, 3)
                .toJagByteBuf()
        buffer.p2Alt2(extendedInfo.id.toInt())
        buffer.p1Alt3(extendedInfo.delay.toInt())
        return buffer
    }
}
