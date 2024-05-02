package net.rsprot.crypto.xtea

import io.netty.buffer.ByteBuf

private const val XTEA_KEY_SIZE: Int = 4
private const val GOLDEN_RATIO = -0x61c88647
private const val ROUNDS = 32
private const val QUAD_ENCODED_SIZE = Int.SIZE_BYTES + Int.SIZE_BYTES

public fun ByteBuf.xteaDecrypt(
    key: IntArray,
    start: Int = readerIndex(),
    end: Int = writerIndex(),
): ByteBuf {
    require(key.size == XTEA_KEY_SIZE) {
        "The XTEA key should be 128 byte long."
    }
    val result = alloc().buffer(readableBytes())
    for (i in 0..<(end - start) / QUAD_ENCODED_SIZE) {
        @Suppress("INTEGER_OVERFLOW")
        var sum = GOLDEN_RATIO * ROUNDS
        var v0 = readInt()
        var v1 = readInt()
        repeat(ROUNDS) {
            v1 -= (v0 shl 4 xor v0.ushr(5)) + v0 xor sum + key[sum.ushr(11) and 3]
            sum -= GOLDEN_RATIO
            v0 -= (v1 shl 4 xor v1.ushr(5)) + v1 xor sum + key[sum and 3]
        }
        result.writeInt(v0)
        result.writeInt(v1)
    }
    return result
        .writeBytes(this, readableBytes())
}
