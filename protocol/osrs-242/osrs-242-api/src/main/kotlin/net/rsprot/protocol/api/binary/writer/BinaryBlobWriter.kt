package net.rsprot.protocol.api.binary.writer

import net.rsprot.protocol.binary.BinaryBlob
import kotlin.jvm.Throws

/**
 * A functional interface for binary blob writers.
 */
public fun interface BinaryBlobWriter {
    /**
     * Attempts to write the [blob] at the path specified by [net.rsprot.protocol.binary.BinaryHeader.path].
     *
     * @return true if the write succeeded, false if there's a lock on it. A lock will temporarily be
     * acquired when a packet group is being written, as it has to update the payload length retroactively
     * as it writes out all the contents of the group.
     * Note that this function can throw errors regarding file writing and should be gracefully handled.
     */
    @Throws(Throwable::class)
    public fun write(blob: BinaryBlob): Boolean
}
