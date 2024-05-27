package net.rsprot.protocol.loginprot.incoming.pow.challenges.sha256.hashing

import java.security.MessageDigest

/**
 * A SHA-256 hash function using the [MessageDigest] implementation.
 * Unlike the default implementation, this one will utilize a thread-local implementation
 * of the [MessageDigest] instances, which are all reset before use.
 *
 * @property digesters the thread-local message digest instances of SHA-256.
 */
public data object ThreadLocalSha256MessageDigestHashFunction : Sha256HashFunction {
    private val digesters =
        ThreadLocal.withInitial {
            MessageDigest.getInstance("SHA-256")
        }

    override fun hash(input: ByteArray): ByteArray {
        val messageDigest = digesters.get()
        messageDigest.reset()
        messageDigest.update(input)
        return messageDigest.digest()
    }
}
