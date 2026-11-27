package net.rsprot.protocol.api.handlers

import net.rsprot.protocol.api.GameMessageCounterProvider
import net.rsprot.protocol.api.MessageQueueProvider
import net.rsprot.protocol.api.implementation.DefaultGameMessageCounterProvider
import net.rsprot.protocol.api.implementation.DefaultMessageQueueProvider
import net.rsprot.protocol.message.IncomingGameMessage
import net.rsprot.protocol.message.OutgoingGameMessage

/**
 * The handlers for incoming game messages.
 * @property incomingGameMessageQueueProvider the queue provider for any incoming game messages
 * @property outgoingGameMessageQueueProvider the queue provider for any outgoing game messages
 * @property gameMessageCounterProvider the message counter provider, used to track the number
 * of incoming messages over the span of one game cycle.
 */
public class GameMessageHandlers
    @JvmOverloads
    public constructor(
        public val incomingGameMessageQueueProvider: MessageQueueProvider<IncomingGameMessage> =
            DefaultMessageQueueProvider(),
        public val outgoingGameMessageQueueProvider: MessageQueueProvider<OutgoingGameMessage> =
            DefaultMessageQueueProvider(),
        public val gameMessageCounterProvider: GameMessageCounterProvider = DefaultGameMessageCounterProvider(),
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as GameMessageHandlers

            if (incomingGameMessageQueueProvider != other.incomingGameMessageQueueProvider) return false
            if (outgoingGameMessageQueueProvider != other.outgoingGameMessageQueueProvider) return false
            if (gameMessageCounterProvider != other.gameMessageCounterProvider) return false

            return true
        }

        override fun hashCode(): Int {
            var result = incomingGameMessageQueueProvider.hashCode()
            result = 31 * result + outgoingGameMessageQueueProvider.hashCode()
            result = 31 * result + gameMessageCounterProvider.hashCode()
            return result
        }

        override fun toString(): String =
            "GameMessageHandlers(" +
                "incomingGameMessageQueueProvider=$incomingGameMessageQueueProvider, " +
                "outgoingGameMessageQueueProvider=$outgoingGameMessageQueueProvider, " +
                "gameMessageCounterProvider=$gameMessageCounterProvider" +
                ")"
    }
