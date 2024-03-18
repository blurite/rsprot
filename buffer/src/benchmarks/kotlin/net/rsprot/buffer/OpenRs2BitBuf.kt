package net.rsprot.buffer

import io.netty.buffer.ByteBuf
import kotlin.math.min

@Suppress("RedundantVisibilityModifier", "unused", "MemberVisibilityCanBePrivate")
public class OpenRs2BitBuf(
    private val buf: ByteBuf,
) : AutoCloseable {
    private var readerIndex: Long = buf.readerIndex().toLong() shl LOG_BITS_PER_BYTE
        private set(value) {
            field = value
            buf.readerIndex((readerIndex shr LOG_BITS_PER_BYTE).toInt())
        }

    private var writerIndex: Long = buf.writerIndex().toLong() shl LOG_BITS_PER_BYTE
        private set(value) {
            field = value
            buf.writerIndex((writerIndex shr LOG_BITS_PER_BYTE).toInt())
        }

    public fun getBoolean(index: Long): Boolean {
        return getBits(index, 1) != 0
    }

    public fun getBit(index: Long): Int {
        return getBits(index, 1)
    }

    public fun getBits(
        index: Long,
        len: Int,
    ): Int {
        require(len in 1..BITS_PER_INT)

        if (index < 0 || (index + len) > capacity()) {
            throw IndexOutOfBoundsException()
        }

        var value = 0

        var remaining = len
        var byteIndex = (index shr LOG_BITS_PER_BYTE).toInt()
        var bitIndex = (index and MASK_BITS_PER_BYTE.toLong()).toInt()

        while (remaining > 0) {
            val n = min(BITS_PER_BYTE - bitIndex, remaining)
            val shift = (BITS_PER_BYTE - (bitIndex + n)) and MASK_BITS_PER_BYTE
            val mask = (1 shl n) - 1

            val v = buf.getUnsignedByte(byteIndex).toInt()
            value = value shl n
            value = value or ((v shr shift) and mask)

            remaining -= n
            byteIndex++
            bitIndex = 0
        }

        return value
    }

    public fun readBoolean(): Boolean {
        return readBits(1) != 0
    }

    public fun readBit(): Int {
        return readBits(1)
    }

    public fun readBits(len: Int): Int {
        checkReadableBits(len)

        val value = getBits(readerIndex, len)
        readerIndex += len
        return value
    }

    public fun skipBits(len: Int): OpenRs2BitBuf {
        checkReadableBits(len)
        readerIndex += len

        return this
    }

    public fun setBoolean(
        index: Long,
        value: Boolean,
    ): OpenRs2BitBuf {
        if (value) {
            setBits(index, 1, 1)
        } else {
            setBits(index, 1, 0)
        }

        return this
    }

    public fun setBit(
        index: Long,
        value: Int,
    ): OpenRs2BitBuf {
        setBits(index, 1, value)

        return this
    }

    public fun setBits(
        index: Long,
        len: Int,
        value: Int,
    ): OpenRs2BitBuf {
        require(len in 1..BITS_PER_INT)

        if (index < 0 || (index + len) > capacity()) {
            throw IndexOutOfBoundsException()
        }

        var remaining = len
        var byteIndex = (index shr LOG_BITS_PER_BYTE).toInt()
        var bitIndex = (index and MASK_BITS_PER_BYTE.toLong()).toInt()

        while (remaining > 0) {
            val n = min(BITS_PER_BYTE - bitIndex, remaining)
            val shift = (BITS_PER_BYTE - (bitIndex + n)) and MASK_BITS_PER_BYTE
            val mask = (1 shl n) - 1

            var v = buf.getUnsignedByte(byteIndex).toInt()
            v = v and (mask shl shift).inv()
            v = v or (((value shr (remaining - n)) and mask) shl shift)
            buf.setByte(byteIndex, v)

            remaining -= n
            byteIndex++
            bitIndex = 0
        }

        return this
    }

    public fun writeBoolean(value: Boolean): OpenRs2BitBuf {
        if (value) {
            writeBits(1, 1)
        } else {
            writeBits(1, 0)
        }

        return this
    }

    public fun writeBit(value: Int): OpenRs2BitBuf {
        writeBits(1, value)

        return this
    }

    public fun writeBits(
        len: Int,
        value: Int,
    ): OpenRs2BitBuf {
        ensureWritable(len.toLong())

        setBits(writerIndex, len, value)
        writerIndex += len

        return this
    }

    public fun writeZero(len: Int): OpenRs2BitBuf {
        writeBits(len, 0)

        return this
    }

    private fun checkReadableBits(len: Int) {
        require(len >= 0)

        if ((readerIndex + len) > writerIndex) {
            throw IndexOutOfBoundsException()
        }
    }

    public fun ensureWritable(len: Long): OpenRs2BitBuf {
        require(len >= 0)

        if ((writerIndex + len) > maxCapacity()) {
            throw IndexOutOfBoundsException()
        }

        val currentByteIndex = writerIndex shr LOG_BITS_PER_BYTE
        val nextByteIndex = (writerIndex + len + MASK_BITS_PER_BYTE) shr LOG_BITS_PER_BYTE

        buf.ensureWritable((nextByteIndex - currentByteIndex).toInt())

        return this
    }

    public fun readableBits(): Long {
        return writerIndex - readerIndex
    }

    public fun writableBits(): Long {
        return capacity() - writerIndex
    }

    public fun maxWritableBits(): Long {
        return maxCapacity() - writerIndex
    }

    public fun capacity(): Long {
        return buf.capacity().toLong() shl LOG_BITS_PER_BYTE
    }

    public fun capacity(len: Long): OpenRs2BitBuf {
        buf.capacity((len shr LOG_BITS_PER_BYTE).toInt())
        return this
    }

    public fun maxCapacity(): Long {
        return buf.maxCapacity().toLong() shl LOG_BITS_PER_BYTE
    }

    public fun isReadable(): Boolean {
        return readerIndex < writerIndex
    }

    public fun isReadable(len: Long): Boolean {
        require(len >= 0)
        return (readerIndex + len) <= writerIndex
    }

    public fun isWritable(): Boolean {
        return writerIndex < capacity()
    }

    public fun isWritable(len: Long): Boolean {
        require(len >= 0)
        return (writerIndex + len) <= capacity()
    }

    public fun readerIndex(): Long {
        return readerIndex
    }

    public fun readerIndex(index: Long): OpenRs2BitBuf {
        if (index < 0 || index > writerIndex) {
            throw IndexOutOfBoundsException()
        }

        readerIndex = index
        return this
    }

    public fun writerIndex(): Long {
        return writerIndex
    }

    public fun writerIndex(index: Long): OpenRs2BitBuf {
        if (index < readerIndex || index > capacity()) {
            throw IndexOutOfBoundsException()
        }

        writerIndex = index
        return this
    }

    public fun clear(): OpenRs2BitBuf {
        readerIndex = 0
        writerIndex = 0
        return this
    }

    override fun close() {
        val bits = (((writerIndex + MASK_BITS_PER_BYTE) and MASK_BITS_PER_BYTE.toLong().inv()) - writerIndex).toInt()
        if (bits != 0) {
            writeZero(bits)
        }

        readerIndex = (readerIndex + MASK_BITS_PER_BYTE) and MASK_BITS_PER_BYTE.toLong().inv()
    }

    private companion object {
        private const val LOG_BITS_PER_BYTE = 3
        private const val BITS_PER_BYTE = 1 shl LOG_BITS_PER_BYTE
        private const val MASK_BITS_PER_BYTE = BITS_PER_BYTE - 1
        private const val BITS_PER_INT = 32
    }
}
