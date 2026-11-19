package net.rsprot.protocol.api

/**
 * The tracker implementation for INetAddresses.
 * This implementation must be thread safe, as it is triggered by all kinds
 * of Netty threads!
 */
public interface InetAddressTracker {
    /**
     * The register function is invoked whenever a channel goes active
     * @param address the address that connected
     */
    public fun register(address: String)

    /**
     * The deregister function is invoked whenever a channel goes inactive
     * @param address the address that disconnected
     */
    public fun deregister(address: String)

    /**
     * Gets the number of active connections for a given address
     * @param address the address to check
     */
    public fun getCount(address: String): Int
}
