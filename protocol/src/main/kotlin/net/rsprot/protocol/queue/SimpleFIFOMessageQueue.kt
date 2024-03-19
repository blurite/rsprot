package net.rsprot.protocol.queue

import net.rsprot.protocol.message.Message
import java.util.Queue
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * A simple message first-in-first-out queue that is backed by [ConcurrentLinkedQueue].
 */
public class SimpleFIFOMessageQueue<T : Message> : MessageQueue<T> {
    private val queue: Queue<T> = ConcurrentLinkedQueue()

    override fun add(
        message: T,
        filterResult: Int,
    ): Boolean {
        return queue.add(message)
    }

    override fun offer(
        message: T,
        filterResult: Int,
    ): Boolean {
        return queue.add(message)
    }

    override fun remove(): T {
        return queue.remove()
    }

    override fun poll(): T? {
        return queue.poll()
    }

    override fun isEmpty(): Boolean {
        return queue.isEmpty()
    }

    override fun size(): Int {
        return queue.size
    }

    override fun clear() {
        queue.clear()
    }

    override fun element(): T {
        return queue.first()
    }

    override fun peek(): T? {
        return queue.firstOrNull()
    }

    override fun iterator(): Iterator<T> {
        return queue.iterator()
    }
}
