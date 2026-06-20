package org.jire.netty.haproxy

import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.util.AttributeKey
import java.net.InetSocketAddress
import java.net.SocketAddress

/**
 * Utilities for working with [HAProxyAttribute]s in Netty channels.
 */
public object HAProxyAttributes {
    /**
     * The Netty lookup [AttributeKey] for the [HAProxyAttribute].
     */
    @JvmStatic
    public val KEY: AttributeKey<HAProxyAttribute> =
        AttributeKey.valueOf(HAProxyAttribute::class.qualifiedName!!)

    /**
     * The Netty lookup [AttributeKey] for the cached non-proxied host address.
     */
    @JvmStatic
    private val SOURCE_ADDRESS_KEY: AttributeKey<String> =
        AttributeKey.valueOf("SOURCE_ADDRESS")

    /**
     * The [HAProxyAttribute] from the channel if present.
     *
     * If the connection is not proxied through [HAProxy](https://en.wikipedia.org/wiki/HAProxy),
     * this will be `null`.
     */
    @JvmStatic
    public var Channel.haproxyAttribute: HAProxyAttribute?
        get() = attr(KEY).get()
        set(value) = attr(KEY).set(value)

    /**
     * The [HAProxyAttribute] from the channel if present.
     *
     * If the connection is not proxied through [HAProxy](https://en.wikipedia.org/wiki/HAProxy),
     * this will be `null`.
     */
    @JvmStatic
    public var ChannelHandlerContext.haproxyAttribute: HAProxyAttribute?
        get() = channel().haproxyAttribute
        set(value) {
            channel().haproxyAttribute = value
        }

    /**
     * The non-proxied host address from the channel if present (cached on first retrieval).
     */
    private var Channel.cachedSourceAddress: String?
        get() = attr(SOURCE_ADDRESS_KEY).get()
        set(value) = attr(SOURCE_ADDRESS_KEY).set(value)

    /**
     * Gets the real source address of the channel.
     *
     * If the connection is proxied through [HAProxy](https://en.wikipedia.org/wiki/HAProxy),
     * this will return the address parsed from the protocol header.
     *
     * Otherwise, it will return the remote address of the channel.
     */
    @JvmStatic
    public val Channel.sourceAddress: SocketAddress
        get() = haproxyAttribute?.sourceAddress ?: remoteAddress()

    /**
     * Gets the real source address of the channel.
     *
     * If the connection is proxied through [HAProxy](https://en.wikipedia.org/wiki/HAProxy),
     * this will return the address parsed from the protocol header.
     *
     * Otherwise, it will return the remote address of the channel.
     */
    @JvmStatic
    public val ChannelHandlerContext.sourceAddress: SocketAddress
        get() = channel().sourceAddress

    /**
     * Gets the real destination address of the channel.
     *
     * If the connection is proxied through [HAProxy](https://en.wikipedia.org/wiki/HAProxy),
     * this will return the address parsed from the protocol header.
     *
     * Otherwise, it will return the local address of the channel.
     */
    @JvmStatic
    public val Channel.destinationAddress: SocketAddress
        get() = haproxyAttribute?.destinationAddress ?: localAddress()

    /**
     * Gets the real destination address of the channel.
     *
     * If the connection is proxied through [HAProxy](https://en.wikipedia.org/wiki/HAProxy),
     * this will return the address parsed from the protocol header.
     *
     * Otherwise, it will return the local address of the channel.
     */
    @JvmStatic
    public val ChannelHandlerContext.destinationAddress: SocketAddress
        get() = channel().destinationAddress

    /**
     * Gets the real source host of the channel.
     */
    @JvmStatic
    public val Channel.sourceHost: String
        get() = getOrCacheSourceHost()

    /**
     * Gets the real source host of the channel, preferring cached variant to avoid
     * allocations if possible.
     */
    private fun Channel.getOrCacheSourceHost(): String {
        val proxyAttribute = this.haproxyAttribute
        if (proxyAttribute != null) {
            return proxyAttribute.sourceHost
        }
        val cachedHost = this.cachedSourceAddress
        if (cachedHost != null) {
            return cachedHost
        }
        val hostString = (remoteAddress() as InetSocketAddress).hostString
        this.cachedSourceAddress = hostString
        return hostString
    }

    /**
     * Gets the real source host of the channel.
     */
    @JvmStatic
    public val ChannelHandlerContext.sourceHost: String
        get() = channel().sourceHost

    /**
     * Gets the real destination host of the channel.
     */
    @JvmStatic
    public val Channel.destinationHost: String
        get() = haproxyAttribute?.destinationHost ?: (localAddress() as InetSocketAddress).hostString

    /**
     * Gets the real destination host of the channel.
     */
    @JvmStatic
    public val ChannelHandlerContext.destinationHost: String
        get() = channel().destinationHost
}
