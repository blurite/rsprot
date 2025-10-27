package net.rsprot.protocol.loginprot.incoming.pow

import net.rsprot.protocol.loginprot.incoming.pow.challenges.ChallengeMetaData
import net.rsprot.protocol.loginprot.incoming.pow.challenges.ChallengeType
import net.rsprot.protocol.loginprot.incoming.util.LoginBlock

/**
 * An interface to return proof of work implementations based on the input ip.
 */
public fun interface ProofOfWorkProvider<T : ChallengeType<MetaData>, in MetaData : ChallengeMetaData> {
    /**
     * Provides a proof of work instance for a given [String].
     * @param inetAddress the IP from which the client is connecting.
     * @param header header the login block header, containing initial information about the
     * request, such as the revision and the client type connecting.
     * @return a proof of work instance that the client needs to solve, or null if it should be skipped
     */
    public fun provide(
        hostAddress: String,
        header: LoginBlock.Header,
    ): ProofOfWork<T, MetaData>?
}
