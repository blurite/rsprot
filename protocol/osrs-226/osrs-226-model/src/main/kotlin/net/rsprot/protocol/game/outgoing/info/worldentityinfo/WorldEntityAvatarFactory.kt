package net.rsprot.protocol.game.outgoing.info.worldentityinfo

import io.netty.buffer.ByteBufAllocator
import net.rsprot.protocol.common.checkCommunicationThread
import net.rsprot.protocol.common.game.outgoing.info.util.ZoneIndexStorage

/**
 * An avatar factory for world entities.
 * This class will be responsible for allocating and releasing world entity avatars,
 * allowing them to be pooled and re-used, if needed.
 * @property avatarRepository the repository keeping track of existing and past world
 * entity avatars.
 */
@Suppress("DuplicatedCode")
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
     * @param fineX the absolute fine x coordinate of the avatar. This can be calculated
     * by doing x * 128 with absolute coord grid values.
     * @param fineY the fine y coordinate (height) of the avatar.
     * @param fineZ the absolute fine x coordinate of the avatar. This can be calculated
     * by doing z * 128 with absolute coord grid values.
     * @param level the height level of the world entity.
     * @param angle the angle to face
     * @return either a new world entity avatar, or a pooled one that has been
     * updated to contain the provided params.
     */
    public fun alloc(
        index: Int,
        sizeX: Int,
        sizeZ: Int,
        fineX: Int,
        fineY: Int,
        fineZ: Int,
        level: Int,
        angle: Int,
    ): WorldEntityAvatar {
        checkCommunicationThread()
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
        require(fineX in 0..2_097_151) {
            "Fine X coordinate cannot be outside of 0..2_097_151 range"
        }
        require(fineY in 0..1023) {
            "Fine Y coordinate cannot be outside of 0..1023 range"
        }
        require(fineX in 0..2_097_151) {
            "Fine Z coordinate cannot be outside of 0..2_097_151 range"
        }
        require(angle in 0..2047) {
            "Angle must be in range of 0..2047"
        }
        return avatarRepository.getOrAlloc(
            index,
            sizeX,
            sizeZ,
            fineX,
            fineY,
            fineZ,
            level,
            angle,
        )
    }

    /**
     * Releases a world entity avatar back into the pool, allowing it to be re-used in the future.
     * @param avatar the world entity avatar to be released.
     */
    public fun release(avatar: WorldEntityAvatar) {
        checkCommunicationThread()
        avatarRepository.release(avatar)
    }
}
