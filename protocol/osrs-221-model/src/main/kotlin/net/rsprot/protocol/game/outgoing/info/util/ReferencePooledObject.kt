package net.rsprot.protocol.game.outgoing.info.util

public interface ReferencePooledObject {
    public fun onAlloc()

    public fun onDealloc()
}
