package net.rsprot.protocol.api

/**
 * A session id generator for new connections.
 * By default, a secure random implementation is used.
 * This session id is further passed back in the login block, and the library
 * will verify to make sure the session id matches.
 */
public interface SessionIdGenerator {
    /**
     * Generates a new session id
     * @param address in case the session id should be based on the address
     */
    public fun generate(address: String): Long
}
