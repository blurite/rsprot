package net.rsprot.protocol.cryptography

import io.netty.buffer.Unpooled
import net.rsprot.buffer.JagByteBuf
import net.rsprot.buffer.extensions.g4
import net.rsprot.buffer.extensions.toJagByteBuf
import java.math.BigInteger

private const val XTEA_KEY_SIZE: Int = 4
private const val GOLDEN_RATIO = -0x61c88647
private const val ROUNDS = 32
private const val QUAD_ENCODED_SIZE = Int.SIZE_BYTES + Int.SIZE_BYTES

public fun JagByteBuf.decipherRsa(
    exp: BigInteger,
    mod: BigInteger,
    size: Int,
): JagByteBuf {
    val bytes = ByteArray(size)
    this.buffer.readBytes(bytes)
    val result =
        BigInteger(bytes)
            .modPow(exp, mod)
            .toByteArray()
    return this
        .buffer
        .alloc()
        .buffer(result.size)
        .writeBytes(result)
        .toJagByteBuf()
}

public fun JagByteBuf.xteaDecrypt(
    key: IntArray,
    start: Int = buffer.readerIndex(),
    end: Int = buffer.writerIndex(),
): JagByteBuf {
    require(key.size == XTEA_KEY_SIZE) {
        "The XTEA key should be 128 byte long."
    }
    val result = Unpooled.buffer(buffer.readableBytes())
    for (i in 0..<(end - start) / QUAD_ENCODED_SIZE) {
        @Suppress("INTEGER_OVERFLOW")
        var sum = GOLDEN_RATIO * ROUNDS
        var v0 = buffer.g4()
        var v1 = buffer.g4()
        repeat(ROUNDS) {
            v1 -= (v0 shl 4 xor v0.ushr(5)) + v0 xor sum + key[sum.ushr(11) and 3]
            sum -= GOLDEN_RATIO
            v0 -= (v1 shl 4 xor v1.ushr(5)) + v1 xor sum + key[sum and 3]
        }
        result.writeInt(v0)
        result.writeInt(v1)
    }
    return result
        .writeBytes(buffer, buffer.readableBytes())
        .toJagByteBuf()
}
