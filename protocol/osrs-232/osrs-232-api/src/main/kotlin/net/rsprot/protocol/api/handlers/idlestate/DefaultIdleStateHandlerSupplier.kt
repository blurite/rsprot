package net.rsprot.protocol.api.handlers.idlestate

import io.netty.handler.timeout.IdleStateHandler
import java.util.concurrent.TimeUnit

public open class DefaultIdleStateHandlerSupplier
    @JvmOverloads
    constructor(
        public val idleTime: Long,
        public val idleTimeUnit: TimeUnit = TimeUnit.SECONDS,
        public val observeOutput: Boolean = true,
    ) : IdleStateHandlerSupplier {
        override fun supply(): IdleStateHandler =
            IdleStateHandler(
                observeOutput,
                idleTime,
                idleTime,
                idleTime,
                idleTimeUnit,
            )

        public object Initial : DefaultIdleStateHandlerSupplier(30)

        public object Login : DefaultIdleStateHandlerSupplier(40)

        public object Game : DefaultIdleStateHandlerSupplier(15)

        public object JS5 : DefaultIdleStateHandlerSupplier(30)
    }
