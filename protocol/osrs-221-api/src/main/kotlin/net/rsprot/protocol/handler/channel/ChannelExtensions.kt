package net.rsprot.protocol.handler.channel

import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
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
