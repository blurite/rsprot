package net.rsprot.protocol.metrics.lock

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

public class TrafficHandlerLock {
    @PublishedApi
    @Volatile
    internal var lock: Any? = null

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
