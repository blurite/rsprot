package org.jire.netty.haproxy

import io.netty.handler.timeout.IdleStateHandler
import java.util.concurrent.TimeUnit

/**
 * The [IdleStateHandler] used in the HAProxy pipeline to close idle connections.
 */
public open class HAProxyIdleStateHandler(
    idleTimeout: Long = DEFAULT_IDLE_TIMEOUT,
    idleTimeoutUnit: TimeUnit = DEFAULT_IDLE_TIMEOUT_UNIT,
) : IdleStateHandler(
        false,
        0,
        0,
        idleTimeout,
        idleTimeoutUnit,
    ) {
    public companion object {
        public const val DEFAULT_IDLE_TIMEOUT: Long = 60
        public val DEFAULT_IDLE_TIMEOUT_UNIT: TimeUnit = TimeUnit.SECONDS
    }
}
