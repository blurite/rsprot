package net.rsprot.protocol.loginprot.incoming.pow.challenges.sha256

import net.rsprot.protocol.loginprot.incoming.pow.challenges.ChallengeType

/**
 * A SHA-256 challenge is a challenge which forces the client to find a hash which
 * has at least [difficulty] number of leading zero bits in the hash.
 * As hashing returns pseudo-random results, as a general rule of thumb, the work
 * needed to solve a challenge doubles with each difficulty increase, since each
 * bit can be either true or false, and the solution must have at least [difficulty]
 * amount of false (zero) bits.
 * Since the requirement is that there are at least [difficulty] amount of leading
 * zero bits, these challenges aren't constrained to only having a single successful
 * answer.
 * @property unknown an unknown byte value that is appended to the start of each
 * base string that needs hashing. The value of this byte is **always** one in OldSchool
 * RuneScape.
 * @property difficulty the difficulty of the challenge, as explained above, is the number
 * of leading zero bits the hash must have for it to be considered successful.
 * The default difficulty in OldSchool RuneScape is 18 as of writing this.
 * When Proof of Work was first introduced, this value was 16.
 * It is possible that the value gets dynamically increased as the pressure increases,
 * or if there are a lot of requests from a single IP.
 * @property salt the salt string that is the bulk of the input to hash.
 * @property id the id of the challenge as identified by the client.
 * @property resultSize the number of bytes the server must have in its socket after sending
 * a SHA-256 challenge request, in order to attempt to verify it.
 */
public class Sha256Challenge(
    public val unknown: Int,
    public val difficulty: Int,
    public val salt: String,
) : ChallengeType<Sha256MetaData> {
    override val id: Int
        get() = 0
    override val resultSize: Int
        get() = Long.SIZE_BYTES

    /**
     * Gets the base string that is part of the input for the hash.
     * A long will be appended to this base string at the end, which will additionally
     * be the solution to the challenge. The full string of baseString + the long is what
     * must result in [difficulty] number of leading zero bits after having been hashed.
     * @return the base string used for the hashing input.
     */
    public fun getBaseString(): String {
        return Integer.toHexString(this.unknown) + Integer.toHexString(this.difficulty) + this.salt
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Sha256Challenge) return false

        if (unknown != other.unknown) return false
        if (difficulty != other.difficulty) return false
        if (salt != other.salt) return false

        return true
    }

    override fun hashCode(): Int {
        var result = unknown
        result = 31 * result + difficulty
        result = 31 * result + salt.hashCode()
        return result
    }

    override fun toString(): String {
        return "Sha256Challenge(" +
            "unknown=$unknown, " +
            "difficulty=$difficulty, " +
            "salt='$salt'" +
            ")"
    }
}
