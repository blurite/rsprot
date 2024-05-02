package net.rsprot.protocol.api

import net.rsprot.protocol.message.Message
import java.util.Queue

public fun interface MessageQueueProvider<T : Message> {
    public fun provide(): Queue<T>
}
