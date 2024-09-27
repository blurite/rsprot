package net.rsprot.protocol.game.outgoing.info.npcinfo

import io.netty.buffer.ByteBufAllocator
import net.rsprot.compression.provider.HuffmanCodecProvider
import net.rsprot.protocol.common.game.outgoing.info.util.ZoneIndexStorage
import net.rsprot.protocol.game.outgoing.info.filter.ExtendedInfoFilter

/**
 * NPC avatar factor is responsible for allocating new avatars for NPCs,
 * or, if possible, re-using old ones that are no longer in use, to avoid generating
 * mass amounts of garbage.
 * @param allocator the byte buffer allocator used to pre-compute bitcodes for this avatar.
 * @param extendedInfoFilter the filter used to determine whether the given NPC can still
 * have extended info blocks written to it, or if we have to utilize a fall-back and tell
 * the client that despite extended info having been flagged, we cannot write it (by writing
 * the flag itself as a zero, so the client reads no further information).
 * @param extendedInfoWriter the client-specific extended info writers for NPC information.
 * @param huffmanCodec the huffman codec is used to compress chat extended info.
 * While NPCs do not currently have any such extended info blocks, the interface requires
 * it be passed in, so we must still provide it.
 * @param zoneIndexStorage the collection that keeps track of npc indices in various zones.
 */
public class NpcAvatarFactory(
    allocator: ByteBufAllocator,
    extendedInfoFilter: ExtendedInfoFilter,
    extendedInfoWriter: List<NpcAvatarExtendedInfoWriter>,
    huffmanCodec: HuffmanCodecProvider,
    zoneIndexStorage: ZoneIndexStorage,
) {
    /**
     * The avatar repository is responsible for keeping track of all avatars, including ones
     * which are no longer in use - but can be used in the future.
     */
    internal val avatarRepository: NpcAvatarRepository =
        NpcAvatarRepository(
            allocator,
            extendedInfoFilter,
            extendedInfoWriter,
            huffmanCodec,
            zoneIndexStorage,
        )

    /**
     * Allocates a new NPC avatar, or re-uses an older cached one if possible.
     *
     * Npc direction table:
     * ```
     * | Id | Client Angle |  Direction |
     * |:--:|:------------:|:----------:|
     * |  0 |      768     | North-West |
     * |  1 |     1024     |    North   |
     * |  2 |     1280     | North-East |
     * |  3 |      512     |    West    |
     * |  4 |     1536     |    East    |
     * |  5 |      256     | South-West |
     * |  6 |       0      |    South   |
     * |  7 |     1792     | South-East |
     * ```
     *
     * @param index the index of the npc in the world
     * @param id the id of the npc in the world, limited to range of 0..16383
     * @param level the height level of the npc
     * @param x the absolute x coordinate of the npc
     * @param z the absolute z coordinate of the npc
     * @param spawnCycle the game cycle on which the npc spawned into the world;
     * for static NPCs, this would always be zero. This is only used by the C++ clients.
     * @param direction the direction that the npc will face on spawn (see table above)
     * @return a npc avatar with the above provided details.
     */
    public fun alloc(
        index: Int,
        id: Int,
        level: Int,
        x: Int,
        z: Int,
        spawnCycle: Int = 0,
        direction: Int = 0,
    ): NpcAvatar =
        avatarRepository.getOrAlloc(
            index,
            id,
            level,
            x,
            z,
            spawnCycle,
            direction,
        )

    /**
     * Releases the avatar back into the repository to be used by other NPCs.
     * @param avatar the avatar to release.
     */
    public fun release(avatar: NpcAvatar) {
        avatarRepository.release(avatar)
    }
}
