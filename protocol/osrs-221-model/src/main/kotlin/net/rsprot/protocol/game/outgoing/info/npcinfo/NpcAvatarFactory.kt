package net.rsprot.protocol.game.outgoing.info.npcinfo

import io.netty.buffer.ByteBufAllocator
import net.rsprot.compression.HuffmanCodec
import net.rsprot.protocol.game.outgoing.info.playerinfo.filter.ExtendedInfoFilter

public class NpcAvatarFactory(
    private val allocator: ByteBufAllocator,
    private val extendedInfoFilter: ExtendedInfoFilter,
    private val extendedInfoWriter: List<NpcAvatarExtendedInfoWriter>,
    private val huffmanCodec: HuffmanCodec,
) {
    public fun alloc(
        index: Int,
        id: Int,
    ): NpcAvatar {
        val extendedInfo =
            NpcAvatarExtendedInfo(
                index,
                extendedInfoFilter,
                extendedInfoWriter,
                allocator,
                huffmanCodec,
            )
        return NpcAvatar(
            index,
            id,
            extendedInfo,
        )
    }
}
