package net.rsprot.protocol.game.outgoing.info.worldentityinfo

import io.netty.buffer.ByteBufAllocator
import net.rsprot.protocol.internal.game.outgoing.info.util.ZoneIndexStorage

/**
 * An avatar factory for world entities.
 * This class will be responsible for allocating and releasing world entity avatars,
 * allowing them to be pooled and re-used, if needed.
 * @property avatarRepository the repository keeping track of existing and past world
 * entity avatars.
 */
public class WorldEntityAvatarFactory(
    allocator: ByteBufAllocator,
    zoneIndexStorage: ZoneIndexStorage,
) {
    internal val avatarRepository: WorldEntityAvatarRepository =
        WorldEntityAvatarRepository(
            allocator,
            zoneIndexStorage,
        )

    /**
     * Allocates a new world entity with the provided arguments.
     * @param index the index of the world entity
     * @param sizeX the width of the world entity in zones (8 tiles/zone)
     * @param sizeZ the height of the world entity in zones (8 tiles/zone)
     * @param x the absolute x coordinate of the world entity where
     * it is being portrayed in the root world.
     * @param z the absolute z coordinate of the world entity where
     * it is being portrayed in the root world.
     * @param level the height level of the world entity.
     * @return either a new world entity avatar, or a pooled one that has been
     * updated to contain the provided params.
     */
    public fun alloc(
        index: Int,
        sizeX: Int,
        sizeZ: Int,
        x: Int,
        z: Int,
        level: Int,
        angle: Int,
    ): WorldEntityAvatar {
        require(index in 0..2047) {
            "World entity index cannot be outside of 0..2047"
        }
        require(sizeX in 0..255) {
            "Size x cannot be outside of 0..255 range"
        }
        require(sizeZ in 0..255) {
            "Size z cannot be outside of 0..255 range"
        }
        require(level in 0..3) {
            "Level cannot be outside of 0..3 range"
        }
        require(x in 0..16383) {
            "X coordinate cannot be outside of 0..16383 range"
        }
        require(z in 0..16383) {
            "Z coordinate cannot be outside of 0..16383 range"
        }
        require(angle in 0..2047) {
            "Angle must be in range of 0..2047"
        }
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

    /**
     * Releases a world entity avatar back into the pool, allowing it to be re-used in the future.
     * @param avatar the world entity avatar to be released.
     */
    public fun release(avatar: WorldEntityAvatar) {
        avatarRepository.release(avatar)
    }
}
