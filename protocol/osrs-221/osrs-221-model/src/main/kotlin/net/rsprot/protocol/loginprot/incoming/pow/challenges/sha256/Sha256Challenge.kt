package net.rsprot.protocol.loginprot.incoming.pow.challenges.sha256

import net.rsprot.buffer.JagByteBuf
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
 * @property version the version of hashcash to use, only `1` is supported.
 * @property difficulty the difficulty of the challenge, as explained above, is the number
 * of leading zero bits the hash must have for it to be considered successful.
 * The default difficulty in OldSchool RuneScape is 18 as of writing this.
 * When Proof of Work was first introduced, this value was 16.
 * It is possible that the value gets dynamically increased as the pressure increases,
 * or if there are a lot of requests from a single IP.
 * @property salt the salt string that is the bulk of the input to hash.
 * @property id the id of the challenge as identified by the client.
 */
public class Sha256Challenge(
    public val version: Int,
    public val difficulty: Int,
    public val salt: String,
) : ChallengeType<Sha256MetaData> {
    override val id: Int
        get() = 0

    override fun encode(buffer: JagByteBuf) {
        buffer.p1(version)
        buffer.p1(difficulty)
        buffer.pjstr(salt, Charsets.UTF_8)
    }

    /**
     * Gets the base string that is part of the input for the hash.
     * A long will be appended to this base string at the end, which will additionally
     * be the solution to the challenge. The full string of baseString + the long is what
     * must result in [difficulty] number of leading zero bits after having been hashed.
     * @return the base string used for the hashing input.
     */
    public fun getBaseString(): String =
        Integer.toHexString(this.version) + Integer.toHexString(this.difficulty) + this.salt

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Sha256Challenge) return false

        if (version != other.version) return false
        if (difficulty != other.difficulty) return false
        if (salt != other.salt) return false

        return true
    }

    override fun hashCode(): Int {
        var result = version
        result = 31 * result + difficulty
        result = 31 * result + salt.hashCode()
        return result
    }

    override fun toString(): String =
        "Sha256Challenge(" +
            "version=$version, " +
            "difficulty=$difficulty, " +
            "salt='$salt'" +
            ")"
}
