package net.rsprot.protocol.api.implementation

import net.rsprot.protocol.api.SessionIdGenerator
import java.net.SocketAddress
import java.security.SecureRandom

/**
 * The default session id generator, using a secure random to generate the ids.
 */
public class DefaultSessionIdGenerator : SessionIdGenerator {
    private val random = SecureRandom()

    override fun generate(address: SocketAddress): Long = random.nextLong()
}
