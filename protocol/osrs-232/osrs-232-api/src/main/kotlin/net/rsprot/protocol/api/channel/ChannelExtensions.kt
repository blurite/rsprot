package net.rsprot.protocol.api.channel

import io.netty.channel.Channel
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPipeline
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.SocketAddress

/**
 * Gets the INetAddress from a socket address.
 * @throws UnsupportedOperationException if the socket address doesn't support an IP address.
 */
public fun SocketAddress.inetAddress(): InetAddress =
    if (this is InetSocketAddress) {
        address
    } else {
        throw UnsupportedOperationException("Only IP addresses supported")
    }

/**
 * Gets the INetAddress from the given channel
 */
public fun Channel.inetAddress(): InetAddress = remoteAddress().inetAddress()

/**
 * Gets the INetAddress from the given channel handler context.
 */
public fun ChannelHandlerContext.inetAddress(): InetAddress = channel().inetAddress()

/**
 * Replaces a channel handler with a new variant.
 */
public inline fun <reified T : ChannelHandler> ChannelPipeline.replace(newHandler: ChannelHandler): ChannelHandler =
    replace(T::class.java, newHandler::class.qualifiedName, newHandler)
