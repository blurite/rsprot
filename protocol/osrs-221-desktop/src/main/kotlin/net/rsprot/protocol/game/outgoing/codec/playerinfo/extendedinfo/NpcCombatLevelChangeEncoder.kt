package net.rsprot.protocol.game.outgoing.codec.playerinfo.extendedinfo

import io.netty.buffer.ByteBufAllocator
import net.rsprot.buffer.JagByteBuf
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprot.compression.HuffmanCodec
import net.rsprot.protocol.internal.game.outgoing.info.encoder.PrecomputedExtendedInfoEncoder
import net.rsprot.protocol.internal.game.outgoing.info.npcinfo.extendedinfo.CombatLevelChange

public class NpcCombatLevelChangeEncoder : PrecomputedExtendedInfoEncoder<CombatLevelChange> {
    override fun precompute(
        alloc: ByteBufAllocator,
        huffmanCodec: HuffmanCodec,
        extendedInfo: CombatLevelChange,
    ): JagByteBuf {
        val buffer =
            alloc
                .buffer(4, 4)
                .toJagByteBuf()
        buffer.p4Alt3(extendedInfo.level)
        return buffer
    }
}
