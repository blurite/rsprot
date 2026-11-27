package net.rsprot.protocol.api.handlers.idlestate

import io.netty.handler.timeout.IdleStateHandler

/**
 * The suppliers which supply [IdleStateHandler]s.
 */
public class IdleStateHandlerSuppliers
    @JvmOverloads
    constructor(
        public val initialSupplier: IdleStateHandlerSupplier =
            DefaultIdleStateHandlerSupplier.Initial,
        public val loginSupplier: IdleStateHandlerSupplier =
            DefaultIdleStateHandlerSupplier.Login,
        public val gameSupplier: IdleStateHandlerSupplier =
            DefaultIdleStateHandlerSupplier.Game,
        public val js5Supplier: IdleStateHandlerSupplier =
            DefaultIdleStateHandlerSupplier.JS5,
    )
