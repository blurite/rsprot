package net.rsprot.protocol.api

import java.net.InetAddress

public interface InetAddressValidator {
    public fun acceptGameConnection(
        address: InetAddress,
        activeGameConnections: Int,
    ): Boolean

    public fun acceptJs5Connection(
        address: InetAddress,
        activeJs5Connections: Int,
    ): Boolean
}
