package net.rsprot.protocol.queue

import net.rsprot.protocol.message.Message
import java.lang.ClassCastException

/**
 * A queue implementation for [messages][net.rsprot.protocol.message.Message],
 * implemented in a [java.util.Queue]-like manner. This implementation does not
 * extend [java.util.Queue] as the [add] function has an additional parameter
 * to pass in information from the [MessageFilter][net.rsprot.protocol.filter.MessageFilter].
 *
 * In the absence of a [MessageFilter][net.rsprot.protocol.filter.MessageFilter],
 * the property `filterResult` will be set to
 * [NO_MESSAGE_FILTER][net.rsprot.protocol.filter.MessageFilter.NO_MESSAGE_FILTER].
 */
public interface MessageQueue<T : Message> : Iterable<T> {
    /**
     * Inserts the specified message into this queue if it is possible to do so
     * immediately without violating capacity restrictions, returning
     * `true` upon success and throwing an [IllegalStateException]
     * if no space is currently available.
     *
     * @param message the message to add
     * @param filterResult the result of the [net.rsprot.protocol.filter.MessageFilter]
     * @return true if the message was added to this queue successfully
     * @throws IllegalStateException if the message cannot be added at this
     *         time due to capacity restrictions
     * @throws ClassCastException if the class of the specified message
     *         prevents it from being added to this queue
     * @throws NullPointerException if the specified message is null and
     *         this queue does not permit null messages
     * @throws IllegalArgumentException if some property of this message
     *         prevents it from being added to this queue
     */
    @Throws(
        IllegalStateException::class,
        ClassCastException::class,
        NullPointerException::class,
        IllegalArgumentException::class,
    )
    public fun add(
        message: T,
        filterResult: Int,
    ): Boolean

    /**
     * Inserts the specified message into this queue if it is possible to do
     * so immediately without violating capacity restrictions.
     * When using a capacity-restricted queue, this method is generally
     * preferable to [add], which can fail to insert a message only
     * by throwing an exception.
     *
     * @param message the message to add
     * @param filterResult the result of the [net.rsprot.protocol.filter.MessageFilter]
     * @return true if the message was added to this queue, else false
     * @throws ClassCastException if the class of the specified message
     *         prevents it from being added to this queue
     * @throws NullPointerException if the specified message is null and
     *         this queue does not permit null messages
     * @throws IllegalArgumentException if some property of this message
     *         prevents it from being added to this queue
     */
    @Throws(
        ClassCastException::class,
        NullPointerException::class,
        IllegalArgumentException::class,
    )
    public fun offer(
        message: T,
        filterResult: Int,
    ): Boolean

    /**
     * Retrieves and removes the head of this queue. This method differs
     * from [poll] only in that it throws an exception if this
     * queue is empty.
     *
     * @return the head of this queue
     * @throws NoSuchElementException if this queue is empty
     */
    @Throws(NoSuchElementException::class)
    public fun remove(): T

    /**
     * Retrieves and removes the head of this queue,
     * or returns null if this queue is empty.
     *
     * @return the head of this queue, or null if this queue is empty
     */
    public fun poll(): T?

    /**
     * Returns true if this collection contains no messages.
     *
     * @return true if this collection contains no messages
     */
    public fun isEmpty(): Boolean

    /**
     * Returns the number of messages in this queue. If this collection
     * contains more than Integer.MAX_VALUE messages, returns
     * Integer.MAX_VALUE.
     *
     * @return the number of messages in this queue
     */
    public fun size(): Int

    /**
     * Removes all the messages from this collection (optional operation).
     * The queue will be empty after this method returns.
     *
     * @throws UnsupportedOperationException if the [clear] operation
     *         is not supported by this queue
     */
    @Throws(UnsupportedOperationException::class)
    public fun clear()

    /**
     * Retrieves, but does not remove, the head of this queue. This method
     * differs from [peek] only in that it throws an exception
     * if this queue is empty.
     *
     * @return the head of this queue
     * @throws NoSuchElementException if this queue is empty
     */
    @Throws(NoSuchElementException::class)
    public fun element(): T

    /**
     * Retrieves, but does not remove, the head of this queue,
     * or returns null if this queue is empty.
     *
     * @return the head of this queue, or null if this queue is empty
     */
    public fun peek(): T?
}
