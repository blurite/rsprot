package net.rsprot.protocol.api.implementation

import net.rsprot.protocol.api.MessageQueueProvider
import net.rsprot.protocol.message.Message
import java.util.Queue
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * The default message queue provider, returning a concurrent linked queue.
 */
public class DefaultMessageQueueProvider<T : Message> : MessageQueueProvider<T> {
    override fun provide(): Queue<T> {
        return ConcurrentLinkedQueue()
    }
}
