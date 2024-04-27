package net.rsprot.protocol.api.channel

import io.netty.channel.Channel
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPipeline
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.SocketAddress

public fun SocketAddress.inetAddress(): InetAddress =
    if (this is InetSocketAddress) {
        address
    } else {
        throw UnsupportedOperationException("Only IP addresses supported")
    }

public fun Channel.inetAddress(): InetAddress = remoteAddress().inetAddress()

public fun ChannelHandlerContext.inetAddress(): InetAddress = channel().inetAddress()

public inline fun <reified T : ChannelHandler> ChannelPipeline.replace(newHandler: ChannelHandler): ChannelHandler =
    replace(T::class.java, newHandler::class.qualifiedName, newHandler)
