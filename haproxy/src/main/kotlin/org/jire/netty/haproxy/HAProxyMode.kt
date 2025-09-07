package org.jire.netty.haproxy

/**
 * Different modes for configuration of the [HAProxyChannelInitializer].
 */
public enum class HAProxyMode {
    /**
     * Always expect and handle the [HAProxy](https://en.wikipedia.org/wiki/HAProxy) protocol.
     * If the protocol is not detected, the connection will be closed.
     */
    ON,

    /**
     * Automatically detect if the [HAProxy](https://en.wikipedia.org/wiki/HAProxy) protocol is used.
     *
     * This is the recommended mode as it supports both proxied and non-proxied connections.
     */
    AUTO,

    /**
     * Never expect or handle the [HAProxy](https://en.wikipedia.org/wiki/HAProxy) protocol.
     */
    OFF,
}
