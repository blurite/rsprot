package net.rsprot.protocol.game.outgoing.info.playerinfo

import java.lang.ref.ReferenceQueue
import java.lang.ref.SoftReference

/**
 * A storage object for player info world details.
 * As these detail objects are fairly large, with each one making several arrays
 * that are thousands in length, it is preferred to pool and re-use these whenever possible.
 * @property queue the soft reference queue holding these objects.
 */
internal class PlayerInfoWorldDetailsStorage {
    private val queue: ReferenceQueue<PlayerInfoWorldDetails> = ReferenceQueue<PlayerInfoWorldDetails>()

    /**
     * Polls a world from the queue, or creates a new one.
     * @param worldId the id of the world to assign to the details.
     * @return an unused world details implementation.
     */
    internal fun poll(worldId: Int): PlayerInfoWorldDetails {
        val next = queue.poll()?.get()
        if (next != null) {
            next.onAlloc(worldId)
            return next
        }
        return PlayerInfoWorldDetails(worldId)
    }

    /**
     * Pushes a world that's now unused back into the queue, allowing it to be re-used
     * by someone else in the future.
     * @param details the object containing the implementation details.
     */
    internal fun push(details: PlayerInfoWorldDetails) {
        val reference = SoftReference(details, queue)
        reference.enqueue()
    }
}
