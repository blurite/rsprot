package net.rsprot.protocol.loginprot.incoming.pow.challenges.sha256

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.loginprot.incoming.pow.challenges.ChallengeVerifier
import net.rsprot.protocol.loginprot.incoming.pow.challenges.sha256.hashing.DefaultSha256MessageDigestHashFunction
import net.rsprot.protocol.loginprot.incoming.pow.challenges.sha256.hashing.Sha256HashFunction

/**
 * The SHA-256 challenge verifier is a replica of the client's implementation of the
 * SHA-256 proof of work verifier.
 * @property hashFunction the function used to hash the input bytes, with the default
 * implementation being the same as the client - making a new MessageDigest object
 * for each hash. These objects are fairly cheap, though.
 */
public class Sha256ChallengeVerifier(
    private val hashFunction: Sha256HashFunction = DefaultSha256MessageDigestHashFunction,
) : ChallengeVerifier<Sha256Challenge> {
    override fun verify(
        result: JagByteBuf,
        challenge: Sha256Challenge,
    ): Boolean {
        val value = result.g8()
        val baseString = challenge.getBaseString()
        val builder = StringBuilder(baseString)
        builder.append(java.lang.Long.toHexString(value))
        val utf8ByteArray =
            builder
                .toString()
                .toByteArray(Charsets.UTF_8)
        val hash = hashFunction.hash(utf8ByteArray)
        return leadingZeros(hash) >= challenge.difficulty
    }

    /**
     * Counts the number of leading zero bits in the [byteArray].
     * @param byteArray the byte array to check for leading zero bits.
     * @return the number of leading zero bits in the byte array.
     */
    private fun leadingZeros(byteArray: ByteArray): Int {
        var numBits = 0
        for (byte in byteArray) {
            val bitCount = leadingZeros(byte)
            numBits += bitCount
            if (bitCount != Byte.SIZE_BITS) {
                break
            }
        }
        return numBits
    }

    /**
     * Gets the number of leading zero bits in the provided [byte].
     * @return the number of leading zero bits in the byte.
     */
    private fun leadingZeros(byte: Byte): Int {
        var value = byte.toInt() and 0xFF
        if (value == 0) {
            return Byte.SIZE_BITS
        }
        var numBits = 0
        while (value and 0x80 == 0) {
            numBits++
            value = value shl 1
        }
        return numBits
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Sha256ChallengeVerifier) return false

        if (hashFunction != other.hashFunction) return false

        return true
    }

    override fun hashCode(): Int {
        return hashFunction.hashCode()
    }

    override fun toString(): String {
        return "Sha256ChallengeVerifier(hashFunction=$hashFunction)"
    }
}
