package net.rsprot.protocol.metrics.lock

import java.util.concurrent.locks.LockSupport
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * A traffic monitor lock, responsible for conditionally synchronizing.
 * We use this implementation to skip the synchronization overhead most of the time.
 * The only time we do use synchronization is when [net.rsprot.protocol.metrics.NetworkTrafficMonitor.resetTransient]
 * function is called, during which we shortly lock the modifications down in order to ensure consistency
 * in the measurements.
 */
public class TrafficMonitorLock {
    @PublishedApi
    @Volatile
    internal var lock: Any? = null

    /**
     * Synchronizes around a newly created [lock] to temporarily prevent
     * any modifications to a [net.rsprot.protocol.metrics.NetworkTrafficMonitor].
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

        // Because the #use method may take a bit of time to execute, we need to wait for any work to be done.
        // While this could be done with more expensive locks, or an atomic integer counter for fast path users,
        // that ends up with a fairly significant overhead, given how much the #use method actually ends up
        // getting called (every connection, every packet in all directions, every dc reason and so on).
        // Rather than give up so much performance at all times, especially on Netty's event loop, we make the
        // assumption that all invocations of #use are relatively cheap and finish in well under a millisecond.
        // As such, we set up the lock so any calls from here on out synchronize in #use, and we wait
        // for that one millisecond to clear up any existing non-synchronized #use calls currently running.
        // In the worst case scenario if this fails, a concurrent modification exception gets thrown due to the
        // logic that invokes the transfer, and no harm is done (as it copies all the memory upfront
        // before resetting any variables).

        // Use a deadline based implementation where any thread interruptions or attempts to wake up
        // don't end up interrupting this "wait for at least 1 millisecond" requirement.
        val deadline = System.nanoTime() + 1_000_000
        while (true) {
            val remaining = deadline - System.nanoTime()
            if (remaining <= 0) break
            LockSupport.parkNanos(remaining)
        }

        try {
            synchronized(lock) {
                block()
            }
        } finally {
            this.lock = null
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
        val lock = this.lock ?: return block()
        return synchronized(lock) {
            block()
        }
    }
}
