package net.rsprot.protocol.message

import io.netty.util.ReferenceCountUtil
import io.netty.util.ReferenceCounted

public interface IncomingMessage : Message {
    /**
     * Safely releases the message and its payload.
     */
    public fun safeRelease() {
        if (this is ReferenceCounted) {
            if (refCnt() > 0) {
                ReferenceCountUtil.safeRelease(this)
            }
        }
    }
}
