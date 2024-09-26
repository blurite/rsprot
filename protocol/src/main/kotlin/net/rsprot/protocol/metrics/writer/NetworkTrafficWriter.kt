package net.rsprot.protocol.metrics.writer

import net.rsprot.protocol.metrics.snapshots.NetworkTrafficSnapshot

public fun interface NetworkTrafficWriter<in T : NetworkTrafficSnapshot, out R> {
    public fun write(snapshot: T): R
}
