package net.rsprot.protocol.loginprot.incoming.pow.challenges

/**
 * A challenge metadata provider is used to generate a metadata necessary to construct a challenge.
 */
public fun interface ChallengeMetaDataProvider<out T : ChallengeMetaData> {
    /**
     * Provides a metadata instance for a challenge, using the ip as the input parameter.
     * @param ip the IP from which the user is connecting to the server.
     * This is provided in case an implementation which scales with the number of requests
     * from a given host is desired.
     * @return the metadata object necessary to construct a challenge.
     */
    public fun provide(ip: Int): T
}
