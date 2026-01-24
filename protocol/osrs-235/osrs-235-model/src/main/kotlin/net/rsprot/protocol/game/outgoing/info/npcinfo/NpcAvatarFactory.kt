package net.rsprot.protocol.game.outgoing.info.npcinfo

import io.netty.buffer.ByteBufAllocator
import net.rsprot.compression.provider.HuffmanCodecProvider
import net.rsprot.protocol.game.outgoing.info.AvatarPriority
import net.rsprot.protocol.game.outgoing.info.filter.ExtendedInfoFilter
import net.rsprot.protocol.internal.RSProtFlags
import net.rsprot.protocol.internal.checkCommunicationThread
import net.rsprot.protocol.internal.game.outgoing.info.util.ZoneIndexStorage

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
 * @param npcInfoProtocolSupplier a supplier for the npc info protocol. This is a cheap hack
 * to get around a circular dependency issue without rewriting a great deal of code.
 */
public class NpcAvatarFactory(
    allocator: ByteBufAllocator,
    extendedInfoFilter: ExtendedInfoFilter,
    extendedInfoWriter: List<NpcAvatarExtendedInfoWriter>,
    huffmanCodec: HuffmanCodecProvider,
    zoneIndexStorage: ZoneIndexStorage,
    npcInfoProtocolSupplier: DeferredNpcInfoProtocolSupplier,
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
            npcInfoProtocolSupplier,
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
     * @param priority the priority group a NPC belongs into. See [NpcInfo.setPriorityCaps] for greater
     * documentation.
     * @param specific if true, the NPC will only render to players that have explicitly marked this
     * NPC's index as specific-visible, anyone else will be unable to see it. If it's false, anyone can
     * see the NPC regardless.
     * @param renderDistance the distance from which the NPC will render by default.
     * Note that for larger distances, the search radius in zones must also be increased
     * to allow it to even find the NPC. The actual distance to compare ends up being
     * max(npc.renderDistance, npcinfo.renderDistance) - picking the highest of the two,
     * while still constraining it to the zone search range.
     * @return a npc avatar with the above provided details.
     */
    @JvmOverloads
    public fun alloc(
        index: Int,
        id: Int,
        level: Int,
        x: Int,
        z: Int,
        spawnCycle: Int = 0,
        direction: Int = 0,
        priority: AvatarPriority = AvatarPriority.NORMAL,
        specific: Boolean = false,
        renderDistance: Int = 15,
    ): NpcAvatar {
        checkCommunicationThread()
        require(index in 0..65534) {
            "Npc avatar index out of bounds: $index"
        }
        require(id in 0..RSProtFlags.npcAvatarMaxId) {
            "Npc id cannot be outside of 0..${RSProtFlags.npcAvatarMaxId} range"
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
        require(direction in 0..7) {
            "Direction must be in range of 0..7"
        }
        require(renderDistance >= 0) {
            "Render distance cannot be negative."
        }
        return avatarRepository.getOrAlloc(
            index,
            id,
            level,
            x,
            z,
            spawnCycle,
            direction,
            priority,
            specific,
            renderDistance,
        )
    }

    /**
     * Releases the avatar back into the repository to be used by other NPCs.
     * @param avatar the avatar to release.
     */
    public fun release(avatar: NpcAvatar) {
        checkCommunicationThread()
        avatarRepository.release(avatar)
    }
}
