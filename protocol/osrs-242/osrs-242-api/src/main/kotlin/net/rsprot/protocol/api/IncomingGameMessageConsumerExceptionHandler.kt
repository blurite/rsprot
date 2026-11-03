package net.rsprot.protocol.api

import net.rsprot.protocol.message.IncomingGameMessage

/**
 * An exception handler for exceptions caught during the invocation of game message
 * consumers for a given session. As the server only calls one function
 * to process all the incoming messages, any one of them could throw an exception
 * half-way through, thus we need a handler to safely deal with exceptions if that
 * were to happen.
 * @param R the receiver of the session object, typically a player.
 */
public fun interface IncomingGameMessageConsumerExceptionHandler<R> {
    /**
     * Triggered whenever an throwable is caught when invoking the incoming
     * game message consumers for all the packets that came in.
     * @param session the session which triggered the exception
     * @param packet the incoming game message that failed to be processed
     * @param cause the throwable being caught. Note that because this catches
     * throwables, it will also catch errors, which likely should be propagated
     * further.
     */
    public fun exceptionCaught(
        session: Session<R>,
        packet: IncomingGameMessage,
        cause: Throwable,
    )
}
