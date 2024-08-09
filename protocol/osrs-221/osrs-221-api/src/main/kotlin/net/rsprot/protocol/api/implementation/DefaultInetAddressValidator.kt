package net.rsprot.protocol.api.implementation

import net.rsprot.protocol.api.InetAddressValidator
import java.net.InetAddress

/**
 * The default validation for a max number of concurrent active connections
 * from a specific INet address, limited to 10 by default.
 */
public class DefaultInetAddressValidator(
    public val limit: Int = MAX_CONNECTIONS,
) : InetAddressValidator {
    override fun acceptGameConnection(
        address: InetAddress,
        activeGameConnections: Int,
    ): Boolean = activeGameConnections < MAX_CONNECTIONS

    override fun acceptJs5Connection(
        address: InetAddress,
        activeJs5Connections: Int,
    ): Boolean = activeJs5Connections < MAX_CONNECTIONS

    private companion object {
        private const val MAX_CONNECTIONS: Int = 10
    }
}
