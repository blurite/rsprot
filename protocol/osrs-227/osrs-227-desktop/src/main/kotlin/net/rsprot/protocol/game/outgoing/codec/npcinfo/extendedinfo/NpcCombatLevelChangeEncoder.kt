package net.rsprot.protocol.game.outgoing.codec.npcinfo.extendedinfo

import io.netty.buffer.ByteBufAllocator
import net.rsprot.buffer.JagByteBuf
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprot.compression.provider.HuffmanCodecProvider
import net.rsprot.protocol.common.game.outgoing.info.encoder.PrecomputedExtendedInfoEncoder
import net.rsprot.protocol.common.game.outgoing.info.npcinfo.extendedinfo.CombatLevelChange

public class NpcCombatLevelChangeEncoder : PrecomputedExtendedInfoEncoder<CombatLevelChange> {
    override fun precompute(
        alloc: ByteBufAllocator,
        huffmanCodecProvider: HuffmanCodecProvider,
        extendedInfo: CombatLevelChange,
    ): JagByteBuf {
        val buffer =
            alloc
                .buffer(4, 4)
                .toJagByteBuf()
        buffer.p4Alt2(extendedInfo.level)
        return buffer
    }
}
