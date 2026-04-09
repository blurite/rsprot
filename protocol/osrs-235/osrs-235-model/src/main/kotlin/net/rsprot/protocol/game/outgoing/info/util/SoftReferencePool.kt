package net.rsprot.protocol.game.outgoing.info.util

import java.lang.ref.ReferenceQueue
import java.lang.ref.SoftReference
import java.util.ArrayDeque
import java.util.IdentityHashMap

/**
 * A first-in-last-out soft-reference-backed object pool.
 * Prefers last ones to enter due to potential cache locality gains.
 *
 * Objects pushed into this pool are wrapped in [SoftReference]s, allowing the JVM
 * to reclaim them under memory pressure. The pool itself stores the wrappers, not
 * the objects directly.
 *
 * This class is not thread-safe.
 */
public class SoftReferencePool<T : Any>(
    public val capacity: Int = 0,
) {
    init {
        require(capacity >= 0) {
            "Capacity must be >= 0"
        }
    }

    /**
     * Queue notified by the JVM when soft references have had their referents cleared.
     */
    private val referenceQueue = ReferenceQueue<T>()

    /**
     * Backing deque of all pooled reference wrappers.
     */
    private val deque = ArrayDeque<SoftReference<T>>(capacity)

    /**
     * Deque used for invalidating the references.
     * Sole purpose of this is to avoid allocating and deallocating
     * new deque, as the method is called quite frequently.
     */
    private val rebuildDeque = ArrayDeque<SoftReference<T>>(capacity)

    /**
     * Identity-based index of live wrappers currently present in [deque].
     *
     * This lets [invalidate] remove dead wrappers in O(n) time without relying on
     * SoftReference equality/hashCode semantics.
     */
    private val entries = IdentityHashMap<SoftReference<T>, Unit>()

    /**
     * Adds [value] into the pool.
     */
    public fun push(value: T) {
        if (capacity == 0) return
        invalidate()
        if (entries.size >= capacity) return
        val entry = SoftReference(value, referenceQueue)
        deque.addLast(entry)
        entries[entry] = Unit
    }

    /**
     * Returns the next live pooled object, or null if none are available.
     *
     * Dead references are discarded lazily as encountered.
     */
    public fun poll(): T? {
        if (capacity == 0) {
            return null
        }
        while (true) {
            val entry = deque.pollLast() ?: return null
            entries.remove(entry)

            val value = entry.get()
            if (value != null) {
                return value
            }
        }
    }

    /**
     * Removes wrappers whose referents have been garbage-collected.
     *
     * This does not force GC. It only cleans up references that the JVM has already
     * cleared and enqueued into [referenceQueue].
     */
    public fun invalidate() {
        if (capacity == 0) return
        var changed = false
        while (true) {
            @Suppress("UNCHECKED_CAST")
            val cleared = referenceQueue.poll() as SoftReference<T>? ?: break
            if (entries.remove(cleared) != null) {
                changed = true
            }
        }

        if (!changed || deque.isEmpty()) {
            return
        }

        val rebuilt = this.rebuildDeque
        while (true) {
            val entry = deque.pollFirst() ?: break
            if (entry in entries) {
                rebuilt.addLast(entry)
            }
        }

        deque.clear()
        deque.addAll(rebuilt)
        rebuilt.clear()
    }

    /**
     * Returns the number of wrappers currently in the pool.
     *
     * Note that some of these may already be dead until [invalidate] or [poll] runs.
     */
    public fun size(): Int = entries.size

    /**
     * Returns true if the pool currently contains no wrappers.
     */
    public fun isEmpty(): Boolean = entries.isEmpty()

    /**
     * Clears all pooled entries, live or dead.
     */
    public fun clear() {
        deque.clear()
        rebuildDeque.clear()
        entries.clear()
        while (referenceQueue.poll() != null) {
            // drain queue
        }
    }
}
