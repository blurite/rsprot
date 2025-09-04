@file:JvmName("ChannelExtensions")

package net.rsprot.protocol.channel

import io.netty.channel.Channel
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPipeline
import org.jire.netty.haproxy.HAProxyAttributes.sourceHost

/**
 * Gets the host address from the given channel.
 */
public fun Channel.hostAddress(): String = sourceHost

/**
 * Gets the host address from the given channel handler context.
 */
public fun ChannelHandlerContext.hostAddress(): String = sourceHost

/**
 * Replaces a channel handler with a new variant.
 */
public inline fun <reified T : ChannelHandler> ChannelPipeline.replace(newHandler: ChannelHandler): ChannelHandler =
    replace(T::class.java, newHandler::class.qualifiedName, newHandler)
