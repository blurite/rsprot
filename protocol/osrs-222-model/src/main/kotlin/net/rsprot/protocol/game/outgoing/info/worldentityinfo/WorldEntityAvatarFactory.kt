package net.rsprot.protocol.game.outgoing.info.worldentityinfo

import io.netty.buffer.ByteBufAllocator

public class WorldEntityAvatarFactory(
    allocator: ByteBufAllocator,
) {
    internal val avatarRepository: WorldEntityAvatarRepository =
        WorldEntityAvatarRepository(
            allocator,
        )

    public fun alloc(
        index: Int,
        sizeX: Int,
        sizeZ: Int,
        x: Int,
        z: Int,
        level: Int,
        angle: Int,
    ): WorldEntityAvatar {
        return avatarRepository.getOrAlloc(
            index,
            sizeX,
            sizeZ,
            x,
            z,
            level,
            angle,
        )
    }

    public fun release(avatar: WorldEntityAvatar) {
        avatarRepository.release(avatar)
    }
}
