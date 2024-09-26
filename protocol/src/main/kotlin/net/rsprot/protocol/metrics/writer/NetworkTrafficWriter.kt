package net.rsprot.protocol.metrics.writer

import net.rsprot.protocol.metrics.snapshots.NetworkTrafficSnapshot

/**
 * A common interface for any network traffic writers.
 * @param T the network traffic snapshot to transcribe.
 * @param R the result of the transcription.
 */
public fun interface NetworkTrafficWriter<in T : NetworkTrafficSnapshot, out R> {
    /**
     * Writes the [snapshot] object into the [R] type.
     * The exact implementation details are up to the implementation, this may directly
     * write to disk, or it may just return the transcription.
     */
    public fun write(snapshot: T): R
}
