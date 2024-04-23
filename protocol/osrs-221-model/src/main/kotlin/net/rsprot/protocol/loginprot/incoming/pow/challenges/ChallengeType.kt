package net.rsprot.protocol.loginprot.incoming.pow.challenges

/**
 * A common binding interface for challenge types.
 * Currently, the client only supports SHA-256 as a challenge, but it is set up to
 * support other types with ease.
 * @param MetaData the metadata necessary to construct a challenge of this type.
 * @property id the id of the challenge, used by the client to identify what challenge
 * solver to use.
 * @property resultSize the number of bytes the server must have in the socket before
 * it can attempt to verify the challenge.
 */
public interface ChallengeType<in MetaData : ChallengeMetaData> {
    public val id: Int
    public val resultSize: Int
}
