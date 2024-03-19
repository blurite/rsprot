package net.rsprot.protocol.queue

import net.rsprot.protocol.message.Message

/**
 * A simple message first-in-first-out queue that is backed by [ArrayDeque].
 */
public class SimpleFIFOMessageQueue<T : Message>(
    initialCapacity: Int,
) : MessageQueue<T> {
    private val arrayDeque: ArrayDeque<T> = ArrayDeque(initialCapacity)

    override fun add(
        message: T,
        filterResult: Int,
    ): Boolean {
        return arrayDeque.add(message)
    }

    override fun offer(
        message: T,
        filterResult: Int,
    ): Boolean {
        return arrayDeque.add(message)
    }

    override fun remove(): T {
        return arrayDeque.removeFirst()
    }

    override fun poll(): T? {
        return arrayDeque.removeFirstOrNull()
    }

    override fun isEmpty(): Boolean {
        return arrayDeque.isEmpty()
    }

    override fun size(): Int {
        return arrayDeque.size
    }

    override fun clear() {
        arrayDeque.clear()
    }

    override fun element(): T {
        return arrayDeque.first()
    }

    override fun peek(): T? {
        return arrayDeque.firstOrNull()
    }

    override fun iterator(): Iterator<T> {
        return arrayDeque.iterator()
    }
}
