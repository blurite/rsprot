package net.rsprot.crypto.rsa

import io.netty.buffer.ByteBuf
import java.math.BigInteger

public fun ByteBuf.decipherRsa(
    exp: BigInteger,
    mod: BigInteger,
    size: Int,
): ByteBuf {
    val bytes = ByteArray(size)
    readBytes(bytes)
    val result =
        BigInteger(bytes)
            .modPow(exp, mod)
            .toByteArray()
    return this
        .alloc()
        .buffer(result.size)
        .writeBytes(result)
}
