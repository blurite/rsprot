package net.rsprot.protocol.loginprot.incoming.pow.challenges.sha256

import net.rsprot.protocol.loginprot.incoming.pow.ProofOfWorkProvider
import net.rsprot.protocol.loginprot.incoming.pow.SingleTypeProofOfWorkProvider

/**
 * A class to wrap the properties of a SHA-256 into a single instance.
 * @property provider the SHA-256 proof of work provider.
 */
@Suppress("MemberVisibilityCanBePrivate")
public class DefaultSha256ProofOfWorkProvider private constructor(
    public val provider: SingleTypeProofOfWorkProvider<Sha256Challenge, Sha256MetaData>,
) : ProofOfWorkProvider<Sha256Challenge, Sha256MetaData> by provider {
    public constructor(
        world: Int,
    ) : this(
        SingleTypeProofOfWorkProvider(
            DefaultSha256MetaDataProvider(world),
            DefaultSha256ChallengeGenerator(),
            Sha256ChallengeVerifier(),
        ),
    )
}
