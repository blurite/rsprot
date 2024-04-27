package net.rsprot.protocol.api.implementation

import net.rsprot.protocol.api.InetAddressValidator
import java.net.InetAddress

public class DefaultInetAddressValidator(
    public val limit: Int = MAX_CONNECTIONS,
) : InetAddressValidator {
    override fun acceptGameConnection(
        address: InetAddress,
        activeGameConnections: Int,
    ): Boolean {
        return activeGameConnections < MAX_CONNECTIONS
    }

    override fun acceptJs5Connection(
        address: InetAddress,
        activeJs5Connections: Int,
    ): Boolean {
        return activeJs5Connections < MAX_CONNECTIONS
    }

    private companion object {
        private const val MAX_CONNECTIONS: Int = 10
    }
}
