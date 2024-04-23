package net.rsprot.protocol.loginprot.incoming.pow

import io.netty.util.AttributeKey
import net.rsprot.protocol.loginprot.incoming.pow.challenges.ChallengeMetaData
import net.rsprot.protocol.loginprot.incoming.pow.challenges.ChallengeType
import net.rsprot.protocol.loginprot.incoming.pow.challenges.ChallengeVerifier

/**
 * Proof of work is a procedure during login to attempt to throttle login requests from a single source,
 * by requiring them to do CPU-bound work before accepting the login.
 * @property challengeType the type of the challenge to require the client to solve
 * @property challengeVerifier the verifier of that challenge, to ensure the client did complete
 * the world successfully
 */
@Suppress("MemberVisibilityCanBePrivate")
public class ProofOfWork<T : ChallengeType<MetaData>, in MetaData : ChallengeMetaData>(
    public val challengeType: T,
    public val challengeVerifier: ChallengeVerifier<T>,
) {
    public companion object {
        public val PROOF_OF_WORK: AttributeKey<ProofOfWork<*, *>> =
            AttributeKey.newInstance("prot_proof_of_work_challenge")
    }
}
