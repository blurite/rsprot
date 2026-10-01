package net.rsprot.protocol.game.outgoing.info.npcinfo

import net.rsprot.protocol.game.outgoing.info.util.SoftReferencePool
import net.rsprot.protocol.internal.RSProtFlags

/**
 * A storage object for npc info world details.
 * As these detail objects are fairly large, with each one making several arrays
 * that are thousands in length, it is preferred to pool and re-use these whenever possible.
 * @property pool the soft reference pool holding these objects.
 */
internal class NpcInfoWorldDetailsStorage {
    private val pool: SoftReferencePool<NpcInfoWorldDetails> =
        SoftReferencePool(
            if (RSProtFlags.infoPooling) NpcAvatarRepository.AVATAR_CAPACITY else 0,
        )

    /**
     * Polls a world from the queue, or creates a new one.
     * @param worldId the id of the world to assign to the details.
     * @return an unused world details implementation.
     */
    internal fun poll(worldId: Int): NpcInfoWorldDetails {
        val next = pool.poll()
        if (next != null) {
            next.onAlloc(worldId)
            return next
        }
        return NpcInfoWorldDetails(worldId)
    }

    /**
     * Pushes a world that's now unused back into the queue, allowing it to be re-used
     * by someone else in the future.
     * @param details the object containing the implementation details.
     */
    internal fun push(details: NpcInfoWorldDetails) {
        details.onDealloc()
        pool.push(details)
    }
}
