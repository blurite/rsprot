package net.rsprot.protocol.loginprot.incoming.pow.challenges

/**
 * A challenge generator used to construct a challenge out of the provided metadata.
 */
public fun interface ChallengeGenerator<in MetaData : ChallengeMetaData, out Type : ChallengeType<MetaData>> {
    /**
     * A function to generate a challenge out of the provided metadata.
     * @param input the metadata input necessary to generate a challenge
     * @return a challenge generated out of the metadata
     */
    public fun generate(input: MetaData): Type
}
