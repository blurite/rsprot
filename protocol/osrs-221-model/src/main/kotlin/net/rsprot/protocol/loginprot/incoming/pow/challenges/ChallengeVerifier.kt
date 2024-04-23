package net.rsprot.protocol.loginprot.incoming.pow.challenges

import net.rsprot.buffer.JagByteBuf

/**
 * A challenge verifier is used to check the work that the client did.
 * The general idea here is that the client has to perform the work N times, where N is
 * pseudo-random, while the server only has to do that same work one time - to verify the
 * result that the client sent. The complexity of the work to perform is configurable by the
 * server.
 * @param T the challenge type to verify
 */
public interface ChallengeVerifier<in T : ChallengeType<*>> {
    /**
     * Verifies the work performed by the client.
     * @param result the byte buffer containing the result sent by the client.
     * @param challenge the challenge to verify using the [result] provided.
     * @return whether the challenge is solved using the [result] provided.
     */
    public fun verify(
        result: JagByteBuf,
        challenge: T,
    ): Boolean
}
