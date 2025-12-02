package net.rsprot.protocol.game.outgoing.info.worldentityinfo

import io.netty.buffer.ByteBufAllocator
import net.rsprot.compression.provider.HuffmanCodecProvider
import net.rsprot.protocol.internal.checkCommunicationThread
import net.rsprot.protocol.internal.game.outgoing.info.util.ZoneIndexStorage

/**
 * An avatar factory for world entities.
 * This class will be responsible for allocating and releasing world entity avatars,
 * allowing them to be pooled and re-used, if needed.
 * @property avatarRepository the repository keeping track of existing and past world
 * entity avatars.
 * @property huffmanCodec the huffman codec is used to compress chat extended info.
 * While NPCs do not currently have any such extended info blocks, the interface requires
 * it be passed in, so we must still provide it.
 */
@Suppress("DuplicatedCode")
public class WorldEntityAvatarFactory(
    allocator: ByteBufAllocator,
    zoneIndexStorage: ZoneIndexStorage,
    extendedInfoWriter: List<WorldEntityAvatarExtendedInfoWriter>,
    huffmanCodec: HuffmanCodecProvider,
) {
    internal val avatarRepository: WorldEntityAvatarRepository =
        WorldEntityAvatarRepository(
            allocator,
            zoneIndexStorage,
            extendedInfoWriter,
            huffmanCodec,
        )

    /**
     * Allocates a new world entity with the provided arguments.
     * @param index the index of the world entity
     * @param id the id of the world entity config
     * @param priority the render priority of the world entity in the scene
     * @param sizeX the width of the world entity in zones (8 tiles/zone)
     * @param sizeZ the height of the world entity in zones (8 tiles/zone)
     * @param fineX the absolute fine x coordinate of the avatar. This can be calculated
     * by doing x * 128 with absolute coord grid values.
     * @param fineZ the absolute fine x coordinate of the avatar. This can be calculated
     * by doing z * 128 with absolute coord grid values.
     * @param projectedLevel the projected root level of the world entity, where it renders.
     * @param activeLevel the level on which the entities in the world entity (instance) appear.
     * @param angle the angle to face
     * @return either a new world entity avatar, or a pooled one that has been
     * updated to contain the provided params.
     */
    public fun alloc(
        index: Int,
        id: Int,
        priority: WorldEntityPriority,
        sizeX: Int,
        sizeZ: Int,
        fineX: Int,
        fineZ: Int,
        projectedLevel: Int,
        activeLevel: Int,
        angle: Int,
    ): WorldEntityAvatar {
        checkCommunicationThread()
        require(index in 0..2047) {
            "World entity index cannot be outside of 0..2047"
        }
        require(id in 0..65535) {
            "World entity id must be in range of 0..65535"
        }
        require(sizeX in 0..255) {
            "Size x cannot be outside of 0..255 range"
        }
        require(sizeZ in 0..255) {
            "Size z cannot be outside of 0..255 range"
        }
        require(projectedLevel in 0..3) {
            "Projected level cannot be outside of 0..3 range"
        }
        require(activeLevel in 0..3) {
            "Active level cannot be outside of 0..3 range"
        }
        require(fineX in 0..2_097_151) {
            "Fine X coordinate cannot be outside of 0..2_097_151 range"
        }
        require(fineX in 0..2_097_151) {
            "Fine Z coordinate cannot be outside of 0..2_097_151 range"
        }
        require(angle in 0..2047) {
            "Angle must be in range of 0..2047"
        }
        return avatarRepository.getOrAlloc(
            index,
            id,
            priority,
            sizeX,
            sizeZ,
            fineX,
            fineZ,
            projectedLevel,
            activeLevel,
            angle,
        )
    }

    /**
     * Allocates a new world entity with the provided arguments.
     * @param index the index of the world entity
     * @param id the id of the world entity config
     * @param priority the render priority of the world entity in the scene
     * @param sizeX the width of the world entity in zones (8 tiles/zone)
     * @param sizeZ the height of the world entity in zones (8 tiles/zone)
     * @param fineX the absolute fine x coordinate of the avatar. This can be calculated
     * by doing x * 128 with absolute coord grid values.
     * @param fineY the fine y coordinate (height) of the avatar. Note that as of revision 226,
     * this property is overwritten by the ground height and has no impact on the perceived
     * height of the world entity.
     * @param fineZ the absolute fine x coordinate of the avatar. This can be calculated
     * by doing z * 128 with absolute coord grid values.
     * @param projectedLevel the projected root level of the world entity, where it renders.
     * @param activeLevel the level on which the entities in the world entity (instance) appear.
     * @param angle the angle to face
     * @return either a new world entity avatar, or a pooled one that has been
     * updated to contain the provided params.
     */
    @Deprecated(
        "Deprecated as fineY is no longer utilized by the client.",
        replaceWith =
            ReplaceWith(
                "alloc(index, id, priority, " +
                    "sizeX, sizeZ, fineX, fineZ, " +
                    "projectedLevel, activeLevel, angle)",
            ),
    )
    public fun alloc(
        index: Int,
        id: Int,
        priority: WorldEntityPriority,
        sizeX: Int,
        sizeZ: Int,
        fineX: Int,
        fineY: Int,
        fineZ: Int,
        projectedLevel: Int,
        activeLevel: Int,
        angle: Int,
    ): WorldEntityAvatar {
        checkCommunicationThread()
        require(index in 0..2047) {
            "World entity index cannot be outside of 0..2047"
        }
        require(id in 0..65535) {
            "World entity id must be in range of 0..65535"
        }
        require(sizeX in 0..255) {
            "Size x cannot be outside of 0..255 range"
        }
        require(sizeZ in 0..255) {
            "Size z cannot be outside of 0..255 range"
        }
        require(projectedLevel in 0..3) {
            "Projected level cannot be outside of 0..3 range"
        }
        require(activeLevel in 0..3) {
            "Active level cannot be outside of 0..3 range"
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
        @Suppress("DEPRECATION")
        return avatarRepository.getOrAlloc(
            index,
            id,
            priority,
            sizeX,
            sizeZ,
            fineX,
            fineY,
            fineZ,
            projectedLevel,
            activeLevel,
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
