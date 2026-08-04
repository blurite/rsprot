package net.rsprot.protocol.game.outgoing.codec.npcinfo.extendedinfo

import io.netty.buffer.ByteBufAllocator
import net.rsprot.buffer.JagByteBuf
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprot.compression.provider.HuffmanCodecProvider
import net.rsprot.protocol.internal.game.outgoing.info.encoder.PrecomputedExtendedInfoEncoder
import net.rsprot.protocol.internal.game.outgoing.info.npcinfo.extendedinfo.NameChange

public class NpcNameChangeEncoder : PrecomputedExtendedInfoEncoder<NameChange> {
    override fun precompute(
        alloc: ByteBufAllocator,
        huffmanCodecProvider: HuffmanCodecProvider,
        extendedInfo: NameChange,
    ): JagByteBuf {
        val text = extendedInfo.name ?: ""
        val capacity = text.length + 1
        val buffer =
            alloc
                .buffer(capacity, capacity)
                .toJagByteBuf()
        buffer.pjstr(text)
        return buffer
    }
}
