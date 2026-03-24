package net.rsprot.protocol.api.js5.util

/**
 * A unique ArrayDeque that utilizes a hash set to check whether something already exists
 * in the queue.
 * This implementation is NOT thread-safe.
 * @property queue the backing array deque
 * @property set the hash set used to check if something exists in this queue
 */
public class UniqueQueue<T> : Iterable<T> {
    private val queue = ArrayDeque<T>()
    private val set = HashSet<T>()

    /**
     * Adds the element [v] into this queue if it isn't already in the hash set.
     * @return true if the element was added
     */
    public fun add(v: T): Boolean {
        if (set.add(v)) {
            queue.addLast(v)
            return true
        }

        return false
    }

    /**
     * Removes the first element from this queue, or null if it doesn't exist.
     * If it does exist, the element is furthermore removed from the backing hash set.
     * @return element if it exists, else null
     */
    public fun removeFirstOrNull(): T? {
        val v = queue.removeFirstOrNull()
        if (v != null) {
            set.remove(v)
            return v
        }

        return null
    }

    /**
     * Clears both the queue and the set.
     */
    public fun clear() {
        queue.clear()
        set.clear()
    }

    override fun iterator(): Iterator<T> = queue.iterator()
}
