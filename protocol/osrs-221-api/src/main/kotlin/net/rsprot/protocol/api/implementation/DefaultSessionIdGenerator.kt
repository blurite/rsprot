package net.rsprot.protocol.api.implementation

import net.rsprot.protocol.api.SessionIdGenerator
import java.net.InetAddress
import java.security.SecureRandom

public class DefaultSessionIdGenerator : SessionIdGenerator {
    private val random = SecureRandom()

    override fun generate(address: InetAddress): Long {
        return random.nextLong()
    }
}
