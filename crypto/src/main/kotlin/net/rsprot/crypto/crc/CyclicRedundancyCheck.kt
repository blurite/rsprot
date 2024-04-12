package net.rsprot.crypto.crc

import io.netty.buffer.ByteBuf

/**
 * A helper object for calculating CRC-32 for a given slice of bytes.
 */
public object CyclicRedundancyCheck {
    /**
     * The 32-bit CRC table.
     */
    private val CRC32 =
        IntArray(256) { index ->
            var crc = index
            for (bit in 0..7) {
                crc =
                    if ((crc and 0x1) == 0x1) {
                        crc ushr 1 xor -0x12477ce0
                    } else {
                        crc ushr 1
                    }
            }
            crc
        }

    /**
     * Computes a 32-bit CRC for the buffer, starting at [start] and ending
     * before [end].
     * @return the 32-bit CRC value for this buffer slice.
     */
    public fun computeCrc32(
        buffer: ByteBuf,
        start: Int,
        end: Int,
    ): Int {
        var checksum = -1
        for (i in start..<end) {
            checksum = checksum ushr 8 xor CRC32[checksum xor buffer.getByte(i).toInt() and 255]
        }
        return checksum.inv()
    }

    /**
     * Computes a 32-bit CRC for the array, starting at zero and ending
     * at the array's length.
     * @return the 32-bit CRC value for this byte array.
     */
    public fun computeCrc32(array: ByteArray): Int {
        var checksum = -1
        for (i in array.indices) {
            checksum = checksum ushr 8 xor CRC32[checksum xor array[i].toInt() and 255]
        }
        return checksum.inv()
    }
}
