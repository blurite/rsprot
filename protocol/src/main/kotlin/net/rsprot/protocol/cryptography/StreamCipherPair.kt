package net.rsprot.protocol.cryptography

import net.rsprot.crypto.cipher.StreamCipher

public data class StreamCipherPair(
    public val encoderCipher: StreamCipher,
    public val decodeCipher: StreamCipher,
)
