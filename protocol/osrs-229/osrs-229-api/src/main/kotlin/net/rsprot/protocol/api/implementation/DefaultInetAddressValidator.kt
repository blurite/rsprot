package net.rsprot.protocol.api.implementation

import net.rsprot.protocol.api.InetAddressValidator
import java.net.SocketAddress

/**
 * The default validation for a max number of concurrent active connections
 * from a specific INet address, limited to 10 by default.
 */
public class DefaultInetAddressValidator(
    public val limit: Int = MAX_CONNECTIONS,
) : InetAddressValidator {
    override fun acceptGameConnection(
        address: SocketAddress,
        activeGameConnections: Int,
    ): Boolean = activeGameConnections < limit

    override fun acceptJs5Connection(
        address: SocketAddress,
        activeJs5Connections: Int,
        seed: IntArray,
    ): Boolean = activeJs5Connections < limit

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DefaultInetAddressValidator

        return limit == other.limit
    }

    override fun hashCode(): Int = limit

    override fun toString(): String = "DefaultInetAddressValidator(limit=$limit)"

    private companion object {
        private const val MAX_CONNECTIONS: Int = 10
    }
}
