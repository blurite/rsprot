package net.rsprot.protocol.game.outgoing.info.util

import net.rsprot.protocol.common.client.OldSchoolClientType

/**
 * An interface used for info protocols for the purpose of re-using an object.
 * This is handy in cases where the objects themselves are heavy, and deallocating and reallocating
 * the object itself might become too costly, so we utilize a soft reference pool to retrieve older
 * objects and re-use them in the future.
 */
public interface ReferencePooledObject {
    /**
     * Invoked whenever a previously pooled object is re-allocated.
     * This function will be responsible for restoring state to be equivalent to newly
     * instantiated object.
     * @param index the index of the new element to allocate.
     * @param oldSchoolClientType the client type used by the new owner.
     */
    public fun onAlloc(
        index: Int,
        oldSchoolClientType: OldSchoolClientType,
    )

    /**
     * Invoked whenever a pooled object is no longer in use.
     * This function is primarily used to clear out any sensitive information or potential memory leaks
     * regarding byte buffers. This function should not fully reset objects, particularly primitives,
     * as there is a chance a given pooled object never gets re-utilized and the garbage collector
     * ends up picking it up. In such cases, it is more beneficial to do the resetting of properties
     * during the [onAlloc], to ensure no work is 'wasted'.
     */
    public fun onDealloc()

    /**
     * Whether this reference pooled object is destroyed.
     * A destroyed object will not be returned back to the pool and instead will be left off for the
     * garbage collector to clean up in due time. This condition is only hit when there was some error
     * thrown during the processing of a given info object. In order to mitigate potential future
     * problems that might continue to stem from re-using this object, we discard it altogether.
     * @return whether the info object is destroyed and should not be returned to the pool.
     */
    public fun isDestroyed(): Boolean
}
