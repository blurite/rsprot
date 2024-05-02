package net.rsprot.crypto.cipher

public data class StreamCipherPair(
    public val encoderCipher: StreamCipher,
    public val decodeCipher: StreamCipher,
)
