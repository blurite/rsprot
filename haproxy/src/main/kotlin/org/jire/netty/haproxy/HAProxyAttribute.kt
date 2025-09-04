package org.jire.netty.haproxy

import io.netty.handler.codec.haproxy.HAProxyProtocolVersion
import io.netty.handler.codec.haproxy.HAProxyProxiedProtocol
import io.netty.util.AbstractReferenceCounted
import io.netty.util.ReferenceCounted
import java.net.InetSocketAddress
import java.net.SocketAddress

/**
 * Holds the attributes parsed from the [HAProxy](https://en.wikipedia.org/wiki/HAProxy) protocol header.
 */
public data class HAProxyAttribute(
    public val version: HAProxyProtocolVersion,
    public val protocol: HAProxyProxiedProtocol,
    public val sourceHost: String,
    public val sourcePort: Int,
    public val destinationHost: String,
    public val destinationPort: Int,
) : AbstractReferenceCounted() {
    public val sourceAddress: SocketAddress =
        InetSocketAddress.createUnresolved(sourceHost, sourcePort)

    public val destinationAddress: SocketAddress =
        InetSocketAddress.createUnresolved(destinationHost, destinationPort)

    override fun touch(hint: Any?): ReferenceCounted = this

    override fun deallocate() {}
}
