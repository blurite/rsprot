package net.rsprot.protocol.api.config

/**
 * A configuration class for various knobs and toggles related to the network.
 * @property incomingGamePacketBacklog the number of incoming game packets that are stored in the decoder
 * and printed as part of exception logs whenever a decoder exception is hit. This allows developers
 * to backtrack the packets and figure out which one caused the problems.
 * The default value is 5 packets.
 */
public class NetworkConfiguration(
    public val incomingGamePacketBacklog: Int,
) {
    /**
     * A builder class to create the network configuration instance.
     */
    public class Builder {
        private var incomingGamePacketBacklog: Int = 5

        /**
         * Sets the number of incoming game packets that are stored in the decoder and printed
         * as part of error stack traces whenever an error in decoding occurs.
         * @param num the number of packet opcodes to store, defaulting to 5.
         */
        public fun setIncomingGamePacketBacklog(num: Int): Builder {
            this.incomingGamePacketBacklog = num
            return this
        }

        /**
         * Builds the network configuration instance.
         */
        internal fun build(): NetworkConfiguration =
            NetworkConfiguration(
                incomingGamePacketBacklog,
            )
    }
}
