package net.rsprot.protocol.api.implementation

import net.rsprot.protocol.api.SessionIdGenerator
import java.net.InetAddress
import java.security.SecureRandom

/**
 * The default session id generator, using a secure random to generate the ids.
 */
public class DefaultSessionIdGenerator : SessionIdGenerator {
    private val random = SecureRandom()

    override fun generate(address: InetAddress): Long = random.nextLong()
}
