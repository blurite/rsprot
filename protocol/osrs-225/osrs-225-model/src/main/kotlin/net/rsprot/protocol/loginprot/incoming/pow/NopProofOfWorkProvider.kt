package net.rsprot.protocol.loginprot.incoming.pow

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.loginprot.incoming.pow.challenges.ChallengeMetaData
import net.rsprot.protocol.loginprot.incoming.pow.challenges.ChallengeType
import java.net.InetAddress

/**
 * A no-operation proof of work provider, allowing one to skip proof of work entirely.
 */
public object NopProofOfWorkProvider :
    ProofOfWorkProvider<NopProofOfWorkProvider.NopChallengeType, NopProofOfWorkProvider.NopChallengeMetaData> {
    override fun provide(inetAddress: InetAddress): ProofOfWork<NopChallengeType, NopChallengeMetaData>? = null

    public object NopChallengeType : ChallengeType<NopChallengeMetaData> {
        override val id: Int = 0

        override fun encode(buffer: JagByteBuf) {
            // nop
        }

        override fun estimateMessageSize(): Int {
            return 0
        }
    }

    public object NopChallengeMetaData : ChallengeMetaData
}
