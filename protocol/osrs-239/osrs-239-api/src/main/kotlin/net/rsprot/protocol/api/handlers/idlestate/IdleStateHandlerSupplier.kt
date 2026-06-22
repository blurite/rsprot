package net.rsprot.protocol.api.handlers.idlestate

import io.netty.handler.timeout.IdleStateHandler

public fun interface IdleStateHandlerSupplier {
    public fun supply(): IdleStateHandler
}
