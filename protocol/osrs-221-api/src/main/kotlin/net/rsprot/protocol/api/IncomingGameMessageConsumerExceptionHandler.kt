package net.rsprot.protocol.api

import net.rsprot.protocol.message.IncomingGameMessage

public fun interface IncomingGameMessageConsumerExceptionHandler<R> {
    public fun exceptionCaught(
        session: Session<R>,
        packet: IncomingGameMessage,
        cause: Throwable,
    )
}
