package net.rsprot.crypto.rsa

import io.netty.buffer.ByteBuf
import java.math.BigInteger

/**
 * Deciphers a slice from this buffer using RSA encryption/decryption.
 * @param exp the exponent to apply to this block of data
 * @param mod the modulo to run against the result of that
 * @param size the number of bytes to read from this buffer for the RSA block
 * @param preferNative whether to prefer a native, faster library for RSA. This library shows ~4.7x
 * performance improvements, but is not supported on all platforms. Any failure to use it will turn off
 * native support and fall back to the Java variant for the rest of the applications runtime.
 * @return a slice of this buffer using this buffer's allocator that is the result of running
 * the modPow operation on the slice.
 * @throws ArithmeticException mod ≤ 0 or the exponent is negative and base is not relatively prime to mod.
 */
public fun ByteBuf.decipherRsa(
    exp: BigInteger,
    mod: BigInteger,
    size: Int,
    preferNative: Boolean = true,
): ByteBuf {
    val bytes = ByteArray(size)
    readBytes(bytes)
    val base = BigInteger(bytes)
    val result =
        Rsa.modPow(
            base,
            exp,
            mod,
            preferNative,
        ).toByteArray()
    return this
        .alloc()
        .buffer(result.size)
        .writeBytes(result)
}
