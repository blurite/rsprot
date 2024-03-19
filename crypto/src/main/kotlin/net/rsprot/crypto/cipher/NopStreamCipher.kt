package net.rsprot.crypto.cipher

/**
 * An implementation-less stream cipher that leaves the stream how it was.
 */
public object NopStreamCipher : StreamCipher {
    override fun nextInt(): Int {
        return 0
    }
}
