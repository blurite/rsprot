package net.rsprot.protocol.loginprot.incoming.pow.challenges.sha256.hashing

import java.security.MessageDigest

/**
 * The default SHA-256 hash function using the [MessageDigest] implementation.
 * Each hash request will generate a new instance of the [MessageDigest] object,
 * as these implementations are not thread safe.
 * These [MessageDigest] instances however are relatively cheap to construct.
 */
public data object DefaultSha256MessageDigestHashFunction : Sha256HashFunction {
    override fun hash(input: ByteArray): ByteArray {
        val messageDigest = MessageDigest.getInstance("SHA-256")
        messageDigest.update(input)
        return messageDigest.digest()
    }
}
