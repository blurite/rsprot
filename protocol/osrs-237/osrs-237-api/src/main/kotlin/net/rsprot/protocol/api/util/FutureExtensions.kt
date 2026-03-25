package net.rsprot.protocol.api.util

import io.netty.util.concurrent.Future
import java.util.concurrent.CompletableFuture

/**
 * Turns a normal Netty future object into a completable future, allowing
 * for easier use of it.
 */
public fun <V> Future<V>.asCompletableFuture(): CompletableFuture<V> {
    if (isDone) {
        return if (isSuccess) {
            CompletableFuture.completedFuture(now)
        } else {
            CompletableFuture.failedFuture(cause())
        }
    }

    val future = CompletableFuture<V>()

    addListener {
        if (isSuccess) {
            future.complete(now)
        } else {
            future.completeExceptionally(cause())
        }
    }

    return future
}
