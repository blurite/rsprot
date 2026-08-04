package net.rsprot.protocol.api

import net.rsprot.protocol.message.Message
import java.util.Queue

/**
 * The queue provider for any type of messages.
 * The queue must be thread-safe!
 * The default implementation is a ConcurrentLinkedQueue.
 * @param T the type of the message that the queue handles, either incoming or outgoing.
 */
public fun interface MessageQueueProvider<T : Message> {
    /**
     * Provides a new instance of the message queue. This should always
     * return a new instance of the queue implementation.
     */
    public fun provide(): Queue<T>
}
