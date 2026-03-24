package net.rsprot.protocol.api.handlers

import net.rsprot.protocol.api.ChannelExceptionHandler
import net.rsprot.protocol.api.IncomingGameMessageConsumerExceptionHandler
import net.rsprot.protocol.api.implementation.DefaultIncomingGameMessageConsumerExceptionHandler

/**
 * A wrapper class for all the exception handlers necessary to make this library function safely.
 * @property channelExceptionHandler the exception handler for any exceptions caught by netty handlers
 * @property incomingGameMessageConsumerExceptionHandler the exception handler for exceptions triggered
 * via any message consumers, in order to allow the message processing to take place safely without
 * the server needing to wrap each payload with its own exception handler
 */
public class ExceptionHandlers<R>
    @JvmOverloads
    public constructor(
        public val channelExceptionHandler: ChannelExceptionHandler,
        public val incomingGameMessageConsumerExceptionHandler: IncomingGameMessageConsumerExceptionHandler<R> =
            DefaultIncomingGameMessageConsumerExceptionHandler(),
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as ExceptionHandlers<*>

            if (channelExceptionHandler != other.channelExceptionHandler) return false
            if (incomingGameMessageConsumerExceptionHandler != other.incomingGameMessageConsumerExceptionHandler) {
                return false
            }

            return true
        }

        override fun hashCode(): Int {
            var result = channelExceptionHandler.hashCode()
            result = 31 * result + incomingGameMessageConsumerExceptionHandler.hashCode()
            return result
        }

        override fun toString(): String =
            "ExceptionHandlers(" +
                "channelExceptionHandler=$channelExceptionHandler, " +
                "incomingGameMessageConsumerExceptionHandler=$incomingGameMessageConsumerExceptionHandler" +
                ")"
    }
