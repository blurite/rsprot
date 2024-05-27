package net.rsprot.protocol.loginprot.incoming.pow.challenges.sha256.hashing

/**
 * A SHA-256 hash function is a function used to turn the input bytes into a valid SHA-256 hash.
 */
public interface Sha256HashFunction {
    /**
     * The hash function takes an [input] byte array and turns it into a valid SHA-256 hash.
     * @param input the input byte array to be hashed.
     * @return the SHA-256 hash.
     */
    public fun hash(input: ByteArray): ByteArray
}
