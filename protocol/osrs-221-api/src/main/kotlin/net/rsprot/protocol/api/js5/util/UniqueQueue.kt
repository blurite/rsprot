package net.rsprot.protocol.api.js5.util

public class UniqueQueue<T> {
    private val queue = ArrayDeque<T>()
    private val set = HashSet<T>()

    public fun add(v: T): Boolean {
        if (set.add(v)) {
            queue.addLast(v)
            return true
        }

        return false
    }

    public fun removeFirstOrNull(): T? {
        val v = queue.removeFirstOrNull()
        if (v != null) {
            set.remove(v)
            return v
        }

        return null
    }

    public fun clear() {
        queue.clear()
        set.clear()
    }
}
