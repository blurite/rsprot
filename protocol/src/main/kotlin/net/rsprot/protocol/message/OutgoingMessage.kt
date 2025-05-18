package net.rsprot.protocol.message

import io.netty.util.ReferenceCountUtil
import io.netty.util.ReferenceCounted

public interface OutgoingMessage : Message {
    /**
     * A function to estimate the size of a message.
     * The estimate should only consist of the payload, not the header.
     */
    public fun estimateSize(): Int = -1

    /**
     * Safely releases the message and its payload.
     * This additionally marks the message as consumed if it is a consumable message.
     */
    public fun safeRelease() {
        markConsumed()
        if (this is ReferenceCounted) {
            if (refCnt() > 0) {
                ReferenceCountUtil.safeRelease(this)
            }
        }
    }

    /**
     * Marks the message as consumed.
     * This assists in dealing with byte buffer leaks. When we mark a message as consumed,
     * we notify the system that this message's buffers are guaranteed to be released in the future,
     * and it does not need to be leak-detected by us.
     */
    public fun markConsumed() {
        if (this is ConsumableMessage) {
            consume()
        }
    }
}
