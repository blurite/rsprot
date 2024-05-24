package net.rsprot.protocol.loginprot.incoming.pow.challenges

import net.rsprot.buffer.JagByteBuf

/**
 * A common binding interface for challenge types.
 * Currently, the client only supports SHA-256 as a challenge, but it is set up to
 * support other types with ease.
 * @param MetaData the metadata necessary to construct a challenge of this type.
 * @property id the id of the challenge, used by the client to identify what challenge
 * solver to use.
 */
public interface ChallengeType<in MetaData : ChallengeMetaData> {
    public val id: Int

    /**
     * A function to encode the given challenge into the byte buffer that the client expects.
     * The role of encoding is moved over to the implementation as the server can provide
     * its own implementations, should the client support any.
     * @param buffer the buffer into which to encode the challenge
     */
    public fun encode(buffer: JagByteBuf)
}
