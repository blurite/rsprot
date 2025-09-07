package net.rsprot.protocol.loginprot.incoming.pow

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.loginprot.incoming.pow.challenges.ChallengeMetaData
import net.rsprot.protocol.loginprot.incoming.pow.challenges.ChallengeType

/**
 * A no-operation proof of work provider, allowing one to skip proof of work entirely.
 */
public object NopProofOfWorkProvider :
    ProofOfWorkProvider<NopProofOfWorkProvider.NopChallengeType, NopProofOfWorkProvider.NopChallengeMetaData> {
    override fun provide(hostAddress: String): ProofOfWork<NopChallengeType, NopChallengeMetaData>? = null

    public object NopChallengeType : ChallengeType<NopChallengeMetaData> {
        override val id: Int = 0

        override fun encode(buffer: JagByteBuf) {
            // nop
        }
    }

    public object NopChallengeMetaData : ChallengeMetaData
}
