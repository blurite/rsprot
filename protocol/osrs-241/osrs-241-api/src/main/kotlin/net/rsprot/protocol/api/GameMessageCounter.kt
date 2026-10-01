package net.rsprot.protocol.api

import net.rsprot.protocol.ClientProtCategory

/**
 * An interface for tracking incoming game messages, in order to avoid
 * decoding and consuming too many messages if the client is flooding us
 * with them.
 * This implementation must be thread safe in the sense that the
 * increment and reset functions could be called concurrently from different
 * threads. The default implementation uses an array for tracking the counts
 * and thus does not need such thread safety here.
 */
public interface GameMessageCounter {
    /**
     * Increments the message counter for the provided client prot
     * category.
     * @param clientProtCategory the category of the incoming packet.
     */
    public fun increment(clientProtCategory: ClientProtCategory)

    /**
     * Whether any of the message categories have reached their limit
     * for maximum number of decoded messages.
     */
    public fun isFull(): Boolean

    /**
     * Resets the tracked counts for the messages.
     */
    public fun reset()
}
