package net.rsprot.protocol.game.outgoing.codec.playerinfo.extendedinfo

import io.netty.buffer.ByteBufAllocator
import net.rsprot.buffer.JagByteBuf
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprot.compression.provider.HuffmanCodecProvider
import net.rsprot.protocol.internal.game.outgoing.info.encoder.PrecomputedExtendedInfoEncoder
import net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.Contrast

public class PlayerContrastEncoder : PrecomputedExtendedInfoEncoder<Contrast> {
    override fun precompute(
        alloc: ByteBufAllocator,
        huffmanCodecProvider: HuffmanCodecProvider,
        extendedInfo: Contrast,
    ): JagByteBuf {
        val buffer =
            alloc
                .buffer(7, 7)
                .toJagByteBuf()
        buffer.p2Alt2(extendedInfo.start.toInt())
        buffer.p2(extendedInfo.end.toInt())
        buffer.p1Alt3(extendedInfo.startContrast.toInt())
        buffer.p1Alt3(extendedInfo.endContrast.toInt())
        buffer.p1Alt3(if (extendedInfo.useStartContrast) 1 else 0)
        return buffer
    }
}
