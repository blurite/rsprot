package net.rsprot.protocol.loginprot.incoming.pow.challenges

import java.util.concurrent.CompletableFuture

/**
 * The default challenge worker will perform the work on the calling thread.
 * The SHA-256 challenges are fairly inexpensive and the overhead of switching threads
 * is similar to the work itself done.
 */
public data object DefaultChallengeWorker : ChallengeWorker {
    public override fun <T : ChallengeType<*>, V : ChallengeVerifier<T>> verify(
        result: Long,
        challenge: T,
        verifier: V,
    ): CompletableFuture<Boolean> {
        return try {
            CompletableFuture.completedFuture(verifier.verify(result, challenge))
        } catch (t: Throwable) {
            CompletableFuture.failedFuture(t)
        }
    }
}
