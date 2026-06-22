package net.rsprot.protocol.game.outgoing.codec.playerinfo.extendedinfo

import io.netty.buffer.ByteBufAllocator
import net.rsprot.buffer.JagByteBuf
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprot.compression.provider.HuffmanCodecProvider
import net.rsprot.protocol.internal.game.outgoing.info.encoder.PrecomputedExtendedInfoEncoder
import net.rsprot.protocol.internal.game.outgoing.info.shared.extendedinfo.Say

public class PlayerSayEncoder : PrecomputedExtendedInfoEncoder<Say> {
    override fun precompute(
        alloc: ByteBufAllocator,
        huffmanCodecProvider: HuffmanCodecProvider,
        extendedInfo: Say,
    ): JagByteBuf {
        val text = extendedInfo.text ?: ""
        val capacity = text.length + 1
        val buffer =
            alloc
                .buffer(capacity, capacity)
                .toJagByteBuf()
        buffer.pjstr(text)
        return buffer
    }
}
