package net.rsprot.protocol.api

/**
 * The validation service for [String].
 * This service is responsible for accepting of rejecting connections based
 * on the number of active connections from said service.
 * It is worth noting that game and JS5 are tracked separately, as each
 * client opened will initiate a request to both.
 * Any connections opened at the very start before either JS5 or
 * game has been decided will not be validated, as it is unclear to which
 * end point they wish to connect. Those sessions will time out after 30 seconds
 * if no decision has been made.
 */
public interface InetAddressValidator {
    /**
     * Whether to accept a game connection from the provided [address]
     * based on the current number of active game connections
     * @param address the address attempting to establish a game connection
     * @param activeGameConnections the number of currently active game connections from that address
     */
    public fun acceptGameConnection(
        address: String,
        activeGameConnections: Int,
    ): Boolean

    /**
     * Whether to accept a JS5 connection from the provided [address]
     * based on the current number of active Js5 connections
     * @param address the address attempting to establish a JS5 connection
     * @param activeJs5Connections the number of currently active JS5 connections from that address
     * @param seed the seed used for reconnections and xtea block decryption.
     */
    public fun acceptJs5Connection(
        address: String,
        activeJs5Connections: Int,
        seed: IntArray,
    ): Boolean
}
