package net.rsprot.protocol.api

import java.net.InetAddress

public interface SessionIdGenerator {
    public fun generate(address: InetAddress): Long
}
