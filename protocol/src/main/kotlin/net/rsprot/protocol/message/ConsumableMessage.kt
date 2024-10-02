package net.rsprot.protocol.message

/**
 * An interface which certain packets, such as the info packets, can implement to
 * leave a marker whether the previous packet that was built was sent out to the client.
 * This can be helpful to ensure integrity of the data, as those packets are stateful
 * and guaranteed to crash with very obscure errors if they are not all written.
 */
public interface ConsumableMessage {
    /**
     * Marks this message as consumed.
     */
    public fun consume()

    /**
     * Checks whether this message has been consumed.
     */
    public fun isConsumed(): Boolean
}
