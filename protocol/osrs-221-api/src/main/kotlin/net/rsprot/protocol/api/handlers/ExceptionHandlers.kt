package net.rsprot.protocol.api.handlers

import net.rsprot.protocol.api.ChannelExceptionHandler
import net.rsprot.protocol.api.IncomingGameMessageConsumerExceptionHandler
import net.rsprot.protocol.api.implementation.DefaultIncomingGameMessageConsumerExceptionHandler

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

        override fun toString(): String {
            return "ExceptionHandlers(" +
                "channelExceptionHandler=$channelExceptionHandler, " +
                "incomingGameMessageConsumerExceptionHandler=$incomingGameMessageConsumerExceptionHandler" +
                ")"
        }
    }
