package net.rsprot.protocol.game.outgoing.info.worldentityinfo

import io.netty.buffer.ByteBufAllocator
import net.rsprot.protocol.common.game.outgoing.info.CoordGrid
import java.lang.ref.ReferenceQueue
import java.lang.ref.SoftReference

public class WorldEntityAvatarRepository internal constructor(
    private val allocator: ByteBufAllocator,
) {
    private val elements: Array<WorldEntityAvatar?> = arrayOfNulls(AVATAR_CAPACITY)
    private val queue: ReferenceQueue<WorldEntityAvatar> = ReferenceQueue<WorldEntityAvatar>()
    private val releasedAvatarQueue: ArrayDeque<WorldEntityAvatar> = ArrayDeque()

    public fun getOrNull(idx: Int): WorldEntityAvatar? {
        return elements[idx]
    }

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
     * Transfers the recently released avatars over to the pool so they can be re-used.
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
        internal const val AVATAR_CAPACITY = 2048
    }
}
