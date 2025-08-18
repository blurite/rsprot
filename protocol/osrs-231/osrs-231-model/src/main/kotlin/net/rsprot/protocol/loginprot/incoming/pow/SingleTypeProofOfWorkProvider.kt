package net.rsprot.protocol.loginprot.incoming.pow

import net.rsprot.protocol.loginprot.incoming.pow.challenges.ChallengeGenerator
import net.rsprot.protocol.loginprot.incoming.pow.challenges.ChallengeMetaData
import net.rsprot.protocol.loginprot.incoming.pow.challenges.ChallengeMetaDataProvider
import net.rsprot.protocol.loginprot.incoming.pow.challenges.ChallengeType
import net.rsprot.protocol.loginprot.incoming.pow.challenges.ChallengeVerifier
import net.rsprot.protocol.loginprot.incoming.util.LoginBlock
import java.net.InetAddress

/**
 * A single type proof of work provider is used to always return proof of work instances
 * of a single specific type.
 * @property metaDataProvider the provider used to return instances of metadata for the
 * challenges.
 * @property challengeGenerator the generator that will create a new proof of work challenge
 * based on the input metadata.
 * @property challengeVerifier the verifier that will check if the answer sent by the client
 * is correct.
 */
public class SingleTypeProofOfWorkProvider<T : ChallengeType<MetaData>, in MetaData : ChallengeMetaData>(
    private val metaDataProvider: ChallengeMetaDataProvider<MetaData>,
    private val challengeGenerator: ChallengeGenerator<MetaData, T>,
    private val challengeVerifier: ChallengeVerifier<T>,
) : ProofOfWorkProvider<T, MetaData> {
    override fun provide(
        inetAddress: InetAddress,
        header: LoginBlock.Header,
    ): ProofOfWork<T, MetaData> {
        val metadata = metaDataProvider.provide(inetAddress, header)
        val challenge = challengeGenerator.generate(metadata)
        return ProofOfWork(challenge, challengeVerifier)
    }
}
