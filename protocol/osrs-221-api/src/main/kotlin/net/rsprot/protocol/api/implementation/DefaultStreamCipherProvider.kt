package net.rsprot.protocol.api.implementation

import net.rsprot.crypto.cipher.IsaacRandom
import net.rsprot.crypto.cipher.StreamCipher
import net.rsprot.protocol.api.StreamCipherProvider

public class DefaultStreamCipherProvider : StreamCipherProvider {
    override fun provide(seed: IntArray): StreamCipher {
        return IsaacRandom(seed)
    }
}
