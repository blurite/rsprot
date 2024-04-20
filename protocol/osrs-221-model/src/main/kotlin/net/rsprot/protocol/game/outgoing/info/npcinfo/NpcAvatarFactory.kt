package net.rsprot.protocol.game.outgoing.info.npcinfo

import io.netty.buffer.ByteBufAllocator
import net.rsprot.compression.HuffmanCodec
import net.rsprot.protocol.game.outgoing.info.playerinfo.filter.ExtendedInfoFilter

@ExperimentalUnsignedTypes
public class NpcAvatarFactory(
    allocator: ByteBufAllocator,
    extendedInfoFilter: ExtendedInfoFilter,
    extendedInfoWriter: List<NpcAvatarExtendedInfoWriter>,
    huffmanCodec: HuffmanCodec,
) {
    internal val avatarRepository: NpcAvatarRepository =
        NpcAvatarRepository(
            allocator,
            extendedInfoFilter,
            extendedInfoWriter,
            huffmanCodec,
        )

    public fun alloc(
        index: Int,
        id: Int,
        level: Int,
        x: Int,
        z: Int,
        spawnCycle: Int = 0,
        direction: Int = 0,
    ): NpcAvatar {
        return avatarRepository.getOrAlloc(
            index,
            id,
            level,
            x,
            z,
            spawnCycle,
            direction,
        )
    }
}
