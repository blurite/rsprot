package net.rsprot.protocol.filter

/**
 * A message filter interface, allowing the user to configure
 * how many incoming messages to accept of a specific type per game cycle,
 * before dropping them.
 */
public interface MessageFilter {
    /**
     * Resets the message filter, making it ready to be used
     * in the next cycle.
     */
    public fun reset()

    /**
     * Check if the message with the given [id] and [size] should be accepted.
     * This function is responsible for incrementing any counters that are
     * implementation-specific along-side returning the result.
     *
     * @param id the opcode of the message.
     * @param size the real size of the message, not the -1 & -2 constants.
     *
     * @return special operation code, telling the packet queue how to handle
     * the messages. Note that [DROP_MESSAGE] will cause the message to be
     * dropped early on in the decoder, which means the incoming bytes will
     * simply be skipped, rather than sliced and decoded.
     */
    public fun accept(
        id: Int,
        size: Int,
    ): Int

    public companion object {
        /**
         * Special constant to denote that no message filter has been implemented.
         */

        public const val NO_MESSAGE_FILTER: Int = -1

        /**
         * Special constant to accept the message and pass it on to
         * the message queue.
         */
        public const val ACCEPT_MESSAGE: Int = 0

        /**
         * Special constant to drop the message early in the decoder,
         * avoiding slicing and decoding of the message to begin with.
         *
         * The Netty message decoder will simply skip over those bytes
         * if this response is received.
         */
        public const val DROP_MESSAGE: Int = 1
    }
}
