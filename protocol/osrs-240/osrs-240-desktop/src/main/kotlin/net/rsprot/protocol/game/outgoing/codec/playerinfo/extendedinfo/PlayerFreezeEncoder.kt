package net.rsprot.protocol.game.outgoing.codec.playerinfo.extendedinfo

import io.netty.buffer.ByteBufAllocator
import net.rsprot.buffer.JagByteBuf
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprot.compression.provider.HuffmanCodecProvider
import net.rsprot.protocol.internal.game.outgoing.info.encoder.PrecomputedExtendedInfoEncoder
import net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.Freeze

public class PlayerFreezeEncoder : PrecomputedExtendedInfoEncoder<Freeze> {
    override fun precompute(
        alloc: ByteBufAllocator,
        huffmanCodecProvider: HuffmanCodecProvider,
        extendedInfo: Freeze,
    ): JagByteBuf {
        val buffer =
            alloc
                .buffer(5, 5)
                .toJagByteBuf()
        buffer.p2Alt3(extendedInfo.delay.toInt())
        buffer.p2Alt3(extendedInfo.duration.toInt())
        buffer.p1Alt1(if (extendedInfo.cancelSequence) 1 else 0)
        return buffer
    }
}
