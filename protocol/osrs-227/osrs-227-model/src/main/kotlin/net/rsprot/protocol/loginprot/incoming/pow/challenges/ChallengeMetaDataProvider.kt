package net.rsprot.protocol.loginprot.incoming.pow.challenges

import net.rsprot.protocol.loginprot.incoming.util.LoginBlock
import java.net.SocketAddress

/**
 * A challenge metadata provider is used to generate a metadata necessary to construct a challenge.
 */
public interface ChallengeMetaDataProvider<out T : ChallengeMetaData> {
    /**
     * Provides a metadata instance for a challenge, using the ip as the input parameter.
     * @param inetAddress the IP from which the user is connecting to the server.
     * This is provided in case an implementation which scales with the number of requests
     * from a given host is desired.
     * @param header the login block header, containing initial information about the
     * request, such as the revision and the client type connecting.
     * @return the metadata object necessary to construct a challenge.
     */
    public fun provide(
        socketAddress: SocketAddress,
        header: LoginBlock.Header,
    ): T
}
