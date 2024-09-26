package net.rsprot.protocol.metrics.lock

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * A traffic handler lock, responsible for conditionally synchronizing.
 * We use this implementation to skip the synchronization overhead most of the time.
 * The only time we do use synchronization is when [net.rsprot.protocol.metrics.NetworkTrafficHandler.resetTransient]
 * function is called, during which we shortly lock the modifications down in order to ensure consistency
 * in the measurements.
 */
public class TrafficHandlerLock {
    @PublishedApi
    @Volatile
    internal var lock: Any? = null

    /**
     * Synchronizes around a newly created [lock] to temporarily prevent
     * any modifications to a [net.rsprot.protocol.metrics.NetworkTrafficHandler].
     * While the [block] executes, the [use] function will not be able to execute.
     * @param block the higher order function to execute with synchronized access.
     */
    @OptIn(ExperimentalContracts::class)
    public inline fun transfer(block: () -> Unit) {
        contract {
            callsInPlace(block, InvocationKind.EXACTLY_ONCE)
        }
        val lock = Any()
        this.lock = lock
        synchronized(lock) {
            try {
                block()
            } finally {
                this.lock = null
            }
        }
    }

    /**
     * Conditionally synchronizes around the [lock] object, only if the lock object is
     * not null. Majority of the time, the lock will be null, which means no synchronization
     * takes place.
     */
    @OptIn(ExperimentalContracts::class)
    public inline fun <R> use(block: () -> R): R {
        contract {
            callsInPlace(block, InvocationKind.EXACTLY_ONCE)
        }
        val lock = this.lock
        if (lock == null) {
            return block()
        }
        return synchronized(lock) {
            block()
        }
    }
}
