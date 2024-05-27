package net.rsprot.protocol.api.implementation

import net.rsprot.crypto.cipher.IsaacRandom
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.api.StreamCipherProvider

/**
 * The default stream cipher provider, returning an instance of the ISAAC random
 * stream cipher based on the input seed.
 */
public class DefaultStreamCipherProvider : StreamCipherProvider {
    override fun provide(seed: IntArray): StreamCipher {
        return IsaacRandom(seed)
    }
}
