package net.rsprot.protocol.game.outgoing.codec.playerinfo.extendedinfo

import io.netty.buffer.ByteBufAllocator
import net.rsprot.buffer.JagByteBuf
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprot.compression.HuffmanCodec
import net.rsprot.protocol.common.game.outgoing.info.encoder.PrecomputedExtendedInfoEncoder
import net.rsprot.protocol.common.game.outgoing.info.playerinfo.extendedinfo.TemporaryMoveSpeed

public class PlayerTemporaryMoveSpeedEncoder : PrecomputedExtendedInfoEncoder<TemporaryMoveSpeed> {
    override fun precompute(
        alloc: ByteBufAllocator,
        huffmanCodec: HuffmanCodec,
        extendedInfo: TemporaryMoveSpeed,
    ): JagByteBuf {
        val buffer =
            alloc
                .buffer(1, 1)
                .toJagByteBuf()
        buffer.p1Alt3(extendedInfo.value)
        return buffer
    }
}
