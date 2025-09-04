package org.jire.netty.haproxy

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelInitializer

/**
 * Utilities for working with [HAProxy](https://en.wikipedia.org/wiki/HAProxy).
 */
public object HAProxy {
    @JvmStatic
    @JvmOverloads
    public fun <C : Channel> ServerBootstrap.childHandlerProxied(
        initializer: ChannelInitializer<C>,
        haProxyMode: HAProxyMode = HAProxyMode.AUTO,
    ): ServerBootstrap =
        apply {
            when (haProxyMode) {
                HAProxyMode.OFF -> {
                    childHandler(
                        initializer,
                    )
                }

                HAProxyMode.ON, HAProxyMode.AUTO -> {
                    childHandler(
                        HAProxyChannelInitializer(initializer),
                    )
                }
            }
        }
}
