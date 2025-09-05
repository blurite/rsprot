package org.jire.netty.haproxy

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelInboundHandler
import org.jire.netty.haproxy.HAProxyIdleStateHandler.Companion.DEFAULT_IDLE_TIMEOUT
import org.jire.netty.haproxy.HAProxyIdleStateHandler.Companion.DEFAULT_IDLE_TIMEOUT_UNIT
import java.util.concurrent.TimeUnit

/**
 * Utilities for working with [HAProxy](https://en.wikipedia.org/wiki/HAProxy).
 */
public object HAProxy {
    @JvmStatic
    @JvmOverloads
    public fun ServerBootstrap.childHandlerProxied(
        childHandler: ChannelInboundHandler,
        mode: HAProxyMode = HAProxyMode.AUTO,
        idleTimeout: Long = DEFAULT_IDLE_TIMEOUT,
        idleTimeoutUnit: TimeUnit = DEFAULT_IDLE_TIMEOUT_UNIT,
    ): ServerBootstrap =
        apply {
            when (mode) {
                HAProxyMode.OFF -> {
                    childHandler(
                        childHandler,
                    )
                }

                HAProxyMode.ON, HAProxyMode.AUTO -> {
                    childHandler(
                        HAProxyChannelInitializer(
                            childHandler,
                            mode,
                            idleTimeout,
                            idleTimeoutUnit,
                        ),
                    )
                }
            }
        }
}
