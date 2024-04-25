package net.rsprot.protocol.message.codec.incoming

import net.rsprot.protocol.message.IncomingMessage
import java.util.function.BiConsumer

public class MessageConsumerRepository<R>(
    public val consumers: Map<Class<out IncomingMessage>, BiConsumer<R, out IncomingMessage>>,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MessageConsumerRepository<*>) return false

        if (consumers != other.consumers) return false

        return true
    }

    override fun hashCode(): Int {
        return consumers.hashCode()
    }

    override fun toString(): String {
        return "MessageConsumerRepository(consumers=$consumers)"
    }
}
