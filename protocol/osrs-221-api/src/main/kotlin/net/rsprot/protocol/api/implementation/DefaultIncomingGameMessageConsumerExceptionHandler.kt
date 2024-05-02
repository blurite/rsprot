package net.rsprot.protocol.api.implementation

import com.github.michaelbull.logging.InlineLogger
import net.rsprot.protocol.api.IncomingGameMessageConsumerExceptionHandler
import net.rsprot.protocol.api.Session
import net.rsprot.protocol.message.IncomingGameMessage

public class DefaultIncomingGameMessageConsumerExceptionHandler<R> : IncomingGameMessageConsumerExceptionHandler<R> {
    override fun exceptionCaught(
        session: Session<R>,
        packet: IncomingGameMessage,
        cause: Throwable,
    ) {
        logger.error(cause) {
            "Exception during consumption of $packet for channel ${session.ctx.channel()}"
        }
        // Propagate errors forward
        if (cause is Error) {
            throw cause
        }
    }

    private companion object {
        private val logger: InlineLogger = InlineLogger()
    }
}
