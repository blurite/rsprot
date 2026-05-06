package net.rsprot.protocol.game.outgoing.codec.playerinfo.extendedinfo

import io.netty.buffer.ByteBufAllocator
import net.rsprot.buffer.JagByteBuf
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprot.compression.provider.HuffmanCodecProvider
import net.rsprot.protocol.internal.game.outgoing.info.encoder.PrecomputedExtendedInfoEncoder
import net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.Face

public class PlayerFaceAngleEncoder : PrecomputedExtendedInfoEncoder<Face> {
    override fun precompute(
        alloc: ByteBufAllocator,
        huffmanCodecProvider: HuffmanCodecProvider,
        extendedInfo: Face,
    ): JagByteBuf {
        val buffer =
            alloc
                .buffer(9, 9)
                .toJagByteBuf()
        buffer.p1Alt2(buildFlag(extendedInfo))
        extendedInfo.encode(buffer)
        return buffer
    }

    private fun buildFlag(extendedInfo: Face): Int {
        var flag = extendedInfo.walkMode
        flag = flag or (extendedInfo.kind.value shl 3)
        if (extendedInfo.instant) {
            flag = flag or (1 shl 6)
        }
        return flag
    }
}
