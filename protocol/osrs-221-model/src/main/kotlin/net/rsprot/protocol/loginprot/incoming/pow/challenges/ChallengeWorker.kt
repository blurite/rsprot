package net.rsprot.protocol.loginprot.incoming.pow.challenges

import net.rsprot.buffer.JagByteBuf
import java.util.concurrent.CompletableFuture

/**
 * A worker is used to perform the verifications of the data sent by the client for our
 * proof of work requests. While the work itself is relatively cheap, servers may wish
 * to perform the work on other threads - this interface allows doing that.
 */
public interface ChallengeWorker {
    /**
     * Verifies the result sent by the client.
     * @param result the byte buffer containing the result data sent by the client
     * @param challenge the challenge the client had to solve
     * @param verifier the verifier used to check the work done by the client for out challenge
     * @return a future object containing the result of the work, or an exception.
     * If the future doesn't return immediately, there will be a 30-second timeout applied to it,
     * after which the work will be concluded failed.
     */
    public fun <T : ChallengeType<*>, V : ChallengeVerifier<T>> verify(
        result: JagByteBuf,
        challenge: T,
        verifier: V,
    ): CompletableFuture<Boolean>
}
