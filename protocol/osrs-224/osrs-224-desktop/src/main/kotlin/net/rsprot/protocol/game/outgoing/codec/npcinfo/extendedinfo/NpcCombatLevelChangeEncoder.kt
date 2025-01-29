package net.rsprot.protocol.game.outgoing.codec.npcinfo.extendedinfo

import io.netty.buffer.ByteBufAllocator
import net.rsprot.buffer.JagByteBuf
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprot.compression.provider.HuffmanCodecProvider
import net.rsprot.protocol.internal.game.outgoing.info.encoder.PrecomputedExtendedInfoEncoder
import net.rsprot.protocol.internal.game.outgoing.info.npcinfo.extendedinfo.CombatLevelChange

public class NpcCombatLevelChangeEncoder : PrecomputedExtendedInfoEncoder<net.rsprot.protocol.internal.game.outgoing.info.npcinfo.extendedinfo.CombatLevelChange> {
    override fun precompute(
	    alloc: ByteBufAllocator,
	    huffmanCodecProvider: HuffmanCodecProvider,
	    extendedInfo: net.rsprot.protocol.internal.game.outgoing.info.npcinfo.extendedinfo.CombatLevelChange,
    ): JagByteBuf {
        val buffer =
            alloc
                .buffer(4, 4)
                .toJagByteBuf()
        buffer.p4Alt1(extendedInfo.level)
        return buffer
    }
}
