package net.rsprot.protocol.message.codec.incoming

import net.rsprot.protocol.message.IncomingGameMessage

public class GameMessageConsumerRepository<R>(
    public val consumers: Map<Class<out IncomingGameMessage>, MessageConsumer<R, IncomingGameMessage>>,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is GameMessageConsumerRepository<*>) return false

        if (consumers != other.consumers) return false

        return true
    }

    override fun hashCode(): Int = consumers.hashCode()

    override fun toString(): String = "GameMessageConsumerRepository(consumers=$consumers)"
}
