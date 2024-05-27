package net.rsprot.protocol.loginprot.incoming.pow

import net.rsprot.protocol.loginprot.incoming.pow.challenges.ChallengeMetaData
import net.rsprot.protocol.loginprot.incoming.pow.challenges.ChallengeType
import java.net.InetAddress

/**
 * An interface to return proof of work implementations based on the input ip.
 */
public fun interface ProofOfWorkProvider<T : ChallengeType<MetaData>, in MetaData : ChallengeMetaData> {
    /**
     * Provides a proof of work instance for a given [inetAddress].
     * @param inetAddress the IP from which the client is connecting.
     * @return a proof of work instance that the client needs to solve, or null if it should be skipped
     */
    public fun provide(inetAddress: InetAddress): ProofOfWork<T, MetaData>?
}
