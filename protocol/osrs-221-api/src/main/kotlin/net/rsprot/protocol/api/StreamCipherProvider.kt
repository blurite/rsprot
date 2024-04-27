package net.rsprot.protocol.api

import net.rsprot.crypto.cipher.StreamCipher

public fun interface StreamCipherProvider {
    public fun provide(seed: IntArray): StreamCipher
}
