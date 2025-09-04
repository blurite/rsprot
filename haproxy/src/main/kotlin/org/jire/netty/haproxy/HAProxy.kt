package org.jire.netty.haproxy

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelInitializer
import org.jire.netty.haproxy.HAProxyIdleStateHandler.Companion.DEFAULT_IDLE_TIMEOUT
import org.jire.netty.haproxy.HAProxyIdleStateHandler.Companion.DEFAULT_IDLE_TIMEOUT_UNIT
import java.util.concurrent.TimeUnit

/**
 * Utilities for working with [HAProxy](https://en.wikipedia.org/wiki/HAProxy).
 */
public object HAProxy {
    @JvmStatic
    @JvmOverloads
    public fun <C : Channel> ServerBootstrap.childHandlerProxied(
        initializer: ChannelInitializer<C>,
        mode: HAProxyMode = HAProxyMode.AUTO,
        idleTimeout: Long = DEFAULT_IDLE_TIMEOUT,
        idleTimeoutUnit: TimeUnit = DEFAULT_IDLE_TIMEOUT_UNIT,
    ): ServerBootstrap =
        apply {
            when (mode) {
                HAProxyMode.OFF -> {
                    childHandler(
                        initializer,
                    )
                }

                HAProxyMode.ON, HAProxyMode.AUTO -> {
                    childHandler(
                        HAProxyChannelInitializer(
                            initializer,
                            mode,
                            idleTimeout,
                            idleTimeoutUnit,
                        ),
                    )
                }
            }
        }
}
