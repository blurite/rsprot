package net.rsprot.protocol.loginprot.incoming.pow.challenges.sha256

import net.rsprot.protocol.loginprot.incoming.pow.challenges.ChallengeMetaData

/**
 * The SHA-256 metadata is what the default SHA-256 implementation requires in order
 * to construct new challenges.
 * @property world the world that the client is connecting to. The world id is the second argument
 * to the string that will be hashed.
 * @property difficulty the difficulty of the challenge, which is the number of leading zero bits
 * that the hash must have for it to be considered successful.
 * @property epochTimeMillis the epoch time milliseconds when the request was made.
 * This value is the very first section of the hash input.
 * @property unknown an unknown byte value - this value is always one in OldSchool RuneScape; it is
 * unclear what it is meant to represent.
 */
public class Sha256MetaData
    @JvmOverloads
    public constructor(
        public val world: Int,
        public val difficulty: Int = 18,
        public val epochTimeMillis: Long = System.currentTimeMillis(),
        public val unknown: Int = 1,
    ) : ChallengeMetaData {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Sha256MetaData) return false

            if (world != other.world) return false
            if (difficulty != other.difficulty) return false
            if (epochTimeMillis != other.epochTimeMillis) return false
            if (unknown != other.unknown) return false

            return true
        }

        override fun hashCode(): Int {
            var result = world
            result = 31 * result + difficulty
            result = 31 * result + epochTimeMillis.hashCode()
            result = 31 * result + unknown
            return result
        }

        override fun toString(): String =
            "Sha256MetaData(" +
                "world=$world, " +
                "difficulty=$difficulty, " +
                "epochTimeMillis=$epochTimeMillis, " +
                "unknown=$unknown" +
                ")"
    }
