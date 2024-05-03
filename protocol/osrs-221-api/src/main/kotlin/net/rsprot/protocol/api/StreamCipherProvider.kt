package net.rsprot.protocol.api

import net.rsprot.crypto.cipher.StreamCipher

/**
 * A provider for stream ciphers, where a new instance is allocated after each successful login.
 */
public fun interface StreamCipherProvider {
    /**
     * Provides a new stream cipher based on the input seed.
     */
    public fun provide(seed: IntArray): StreamCipher
}
