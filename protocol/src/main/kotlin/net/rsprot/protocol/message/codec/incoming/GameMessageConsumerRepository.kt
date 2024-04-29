package net.rsprot.protocol.message.codec.incoming

import net.rsprot.protocol.message.IncomingGameMessage
import java.util.function.BiConsumer

public class GameMessageConsumerRepository<R>(
    public val consumers: Map<Class<out IncomingGameMessage>, BiConsumer<R, in IncomingGameMessage>>,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is GameMessageConsumerRepository<*>) return false

        if (consumers != other.consumers) return false

        return true
    }

    override fun hashCode(): Int {
        return consumers.hashCode()
    }

    override fun toString(): String {
        return "GameMessageConsumerRepository(consumers=$consumers)"
    }
}
