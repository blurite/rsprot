package net.rsprot.protocol.game.outgoing.info.worldentityinfo

import io.netty.buffer.ByteBufAllocator
import net.rsprot.protocol.common.game.outgoing.info.CoordGrid
import java.lang.ref.ReferenceQueue
import java.lang.ref.SoftReference

/**
 * An avatar repository for world entities, keeping track of every current avatar,
 * as well as any avatars that were previously used but now released.
 * @property allocator an allocator for the world entity avatars, to be used for
 * precomputed high resolution blocks.
 * @property elements the array of existing world entity avatars, currently in use.
 * @property queue the soft reference queue of world avatars that were previously in use.
 * As a soft reference queue, it will hold on-to the unused references until the JVM
 * absolutely needs the memory - before that, these can be reused, making it a perfect
 * use case for the pooling mechanism.
 * @property releasedAvatarQueue the avatars that were released within this cycle.
 * These avatars are initially put into a different structure as we cannot immediately
 * release them - they could be picked up by something else the same cycle, which could
 * lead to some weird bugs occurring. Instead, we wait for one cycle to pass before
 * pushing them to the queue to be re-usable. This ensures no one is relying on this
 * same instance still.
 */
public class WorldEntityAvatarRepository internal constructor(
    private val allocator: ByteBufAllocator,
) {
    private val elements: Array<WorldEntityAvatar?> = arrayOfNulls(AVATAR_CAPACITY)
    private val queue: ReferenceQueue<WorldEntityAvatar> = ReferenceQueue<WorldEntityAvatar>()
    private val releasedAvatarQueue: ArrayDeque<WorldEntityAvatar> = ArrayDeque()

    /**
     * Gets a world entity at the provided [idx], or null if it doesn't exist.
     * @throws ArrayIndexOutOfBoundsException if the [idx] is < 0, or >= [AVATAR_CAPACITY].
     */
    @Throws(ArrayIndexOutOfBoundsException::class)
    public fun getOrNull(idx: Int): WorldEntityAvatar? = elements[idx]

    /**
     * Gets an existing world entity avatar from the queue if one is ready, or constructs
     * a new avatar if not.
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
    public fun getOrAlloc(
        index: Int,
        sizeX: Int,
        sizeZ: Int,
        x: Int,
        z: Int,
        level: Int,
        angle: Int,
    ): WorldEntityAvatar {
        val existing = queue.poll()?.get()
        if (existing != null) {
            existing.index = index
            existing.sizeX = sizeX
            existing.sizeZ = sizeZ
            existing.currentCoord = CoordGrid(level, x, z)
            existing.lastCoord = existing.currentCoord
            existing.angle = angle
            elements[index] = existing
            return existing
        }
        val avatar =
            WorldEntityAvatar(
                allocator,
                index,
                sizeX,
                sizeZ,
                CoordGrid(level, x, z),
                angle,
            )
        elements[index] = avatar
        return avatar
    }

    /**
     * Releases avatar back into the pool for it to be used later in the future, if possible.
     * @param avatar the avatar to release.
     */
    public fun release(avatar: WorldEntityAvatar) {
        this.elements[avatar.index] = null
        releasedAvatarQueue += avatar
    }

    /**
     * Transfers the recently released avatars over to the pool, so they can be re-used.
     */
    internal fun transferAvatars() {
        if (releasedAvatarQueue.isEmpty()) {
            return
        }
        while (releasedAvatarQueue.isNotEmpty()) {
            val avatar = releasedAvatarQueue.removeFirst()
            val reference = SoftReference(avatar, queue)
            reference.enqueue()
        }
    }

    internal companion object {
        /**
         * The maximum number of world entity avatars.
         */
        internal const val AVATAR_CAPACITY = 2048
    }
}
