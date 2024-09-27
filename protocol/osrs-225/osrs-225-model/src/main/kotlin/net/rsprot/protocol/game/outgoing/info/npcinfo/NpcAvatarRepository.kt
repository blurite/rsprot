package net.rsprot.protocol.game.outgoing.info.npcinfo

import io.netty.buffer.ByteBufAllocator
import net.rsprot.compression.provider.HuffmanCodecProvider
import net.rsprot.protocol.common.game.outgoing.info.CoordGrid
import net.rsprot.protocol.common.game.outgoing.info.npcinfo.NpcAvatarDetails
import net.rsprot.protocol.common.game.outgoing.info.util.ZoneIndexStorage
import net.rsprot.protocol.game.outgoing.info.filter.ExtendedInfoFilter
import java.lang.ref.ReferenceQueue
import java.lang.ref.SoftReference

/**
 * The NPC avatar repository is a class responsible for keeping track of all the avatars
 * in the game, as well as allocating/re-using new instances if needed.
 * @property allocator the byte buffer allocator used to pre-compute bitcodes for an avatar.
 * @property extendedInfoFilter the filter used to determine whether the given NPC can still
 * have extended info blocks written to it, or if we have to utilize a fall-back and tell
 * the client that despite extended info having been flagged, we cannot write it (by writing
 * the flag itself as a zero, so the client reads no further information).
 * @property extendedInfoWriter the client-specific extended info writers for NPC information.
 * @property huffmanCodec the huffman codec is used to compress chat extended info.
 * While NPCs do not currently have any such extended info blocks, the interface requires
 * it be passed in, so we must still provide it.
 * @property zoneIndexStorage the zone index storage responsible for tracking all the NPCs
 * based on the zones in which they lie.
 */
internal class NpcAvatarRepository(
    private val allocator: ByteBufAllocator,
    private val extendedInfoFilter: ExtendedInfoFilter,
    private val extendedInfoWriter: List<NpcAvatarExtendedInfoWriter>,
    private val huffmanCodec: HuffmanCodecProvider,
    private val zoneIndexStorage: ZoneIndexStorage,
) {
    /**
     * The array of npc avatars that currently exist in the game.
     */
    private val elements: Array<NpcAvatar?> = arrayOfNulls(AVATAR_CAPACITY)

    /**
     * A soft-reference queue of avatars that are no longer in use.
     * If the server requires the memory, these references will be freed up, but this is
     * only as a last resort. Other than that, these instances should remain available
     * for a long period of time - rightfully so as extended info blocks primarily
     * are the heavy part.
     */
    private val queue: ReferenceQueue<NpcAvatar> = ReferenceQueue<NpcAvatar>()

    /**
     * Gets a npc avatar at the provided index, or null if it doesn't exist yet.
     * @param idx the index of the avatar to obtain
     * @return the npc avatar, or null if it doesn't exist
     * @throws ArrayIndexOutOfBoundsException if the [idx] is below 0, or >= [AVATAR_CAPACITY]
     */
    fun getOrNull(idx: Int): NpcAvatar? = elements[idx]

    /**
     * Gets an older avatar, or makes a new one depending on the circumstances.
     * If using an older one, this function is responsible for sanitizing the older avatar
     * so that it is equal to a new instance.
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
    fun getOrAlloc(
        index: Int,
        id: Int,
        level: Int,
        x: Int,
        z: Int,
        spawnCycle: Int = 0,
        direction: Int = 0,
    ): NpcAvatar {
        val existing = queue.poll()?.get()
        if (existing != null) {
            existing.resetObservers()
            val details = existing.details
            resetTransientDetails(details)
            details.index = index
            details.id = id
            details.currentCoord = CoordGrid(level, x, z)
            details.spawnCycle = spawnCycle
            details.direction = direction
            details.allocateCycle = NpcInfoProtocol.cycleCount
            zoneIndexStorage.add(index, details.currentCoord)
            elements[index] = existing
            return existing
        }
        val extendedInfo =
            NpcAvatarExtendedInfo(
                index,
                extendedInfoFilter,
                extendedInfoWriter,
                allocator,
                huffmanCodec,
            )
        val avatar =
            NpcAvatar(
                index,
                id,
                level,
                x,
                z,
                spawnCycle,
                direction,
                NpcInfoProtocol.cycleCount,
                extendedInfo,
                zoneIndexStorage,
            )
        zoneIndexStorage.add(index, avatar.details.currentCoord)
        elements[index] = avatar
        return avatar
    }

    /**
     * Releases avatar back into the pool for it to be used later in the future, if possible.
     * @param avatar the avatar to release.
     */
    fun release(avatar: NpcAvatar) {
        zoneIndexStorage.remove(avatar.details.index, avatar.details.currentCoord)
        this.elements[avatar.details.index] = null
        avatar.extendedInfo.reset()
        val reference = SoftReference(avatar, queue)
        reference.enqueue()
    }

    /**
     * Resets all the transient properties with the default values.
     * @param details the npc avatar details class holding all the properties of a NPC.
     */
    private fun resetTransientDetails(details: NpcAvatarDetails) {
        details.stepCount = 0
        details.firstStep = -1
        details.secondStep = -1
        details.movementType = 0
        details.inaccessible = false
    }

    internal companion object {
        internal const val AVATAR_CAPACITY = 65536
    }
}
