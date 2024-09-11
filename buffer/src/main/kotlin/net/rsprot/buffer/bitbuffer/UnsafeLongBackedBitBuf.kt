package net.rsprot.buffer.bitbuffer

@Suppress("NOTHING_TO_INLINE", "unused")
public class UnsafeLongBackedBitBuf {
    internal var value: Long = 0L
        private set
    private var writerIndex: Int = 0
    private var readerIndex: Int = 0

    private inline fun bitmask(pos: Int): Int = (1 shl pos) - 1

    private inline fun longBitmask(pos: Int): Long = (1L shl pos) - 1

    public fun pBits(
        count: Int,
        value: Int,
    ) {
        val bitmask = longBitmask(count)
        val cur = this.value
        val bitpos = LONG_SIZE_BITS - (this.writerIndex and MASK_BITS_PER_LONG)
        val offset = bitpos - count
        val sanitized = (cur and (bitmask shl offset).inv())
        val addition = (value.toLong() and bitmask) shl offset
        this.value = sanitized or addition
        this.writerIndex += count
    }

    public fun gBits(count: Int): Int {
        val bitmask = longBitmask(count)
        val cur = this.value
        val bitpos = LONG_SIZE_BITS - (readerIndex and MASK_BITS_PER_LONG)
        val result = (cur ushr (bitpos - count) and bitmask).toInt()
        this.readerIndex += count
        return result
    }

    public fun gBitsLong(
        offset: Int,
        count: Int,
    ): Long {
        val bitmask = longBitmask(count)
        val cur = this.value
        val bitpos = LONG_SIZE_BITS - (offset and MASK_BITS_PER_LONG)
        return cur ushr (bitpos - count) and bitmask
    }

    public fun isReadable(): Boolean = readerIndex < writerIndex

    public fun isReadable(count: Int): Boolean = (readerIndex + count) <= writerIndex

    public fun readableBits(): Int = writerIndex - readerIndex

    public fun isWritable(): Boolean = writerIndex < Long.SIZE_BITS

    public fun isWritable(len: Int): Boolean = (writerIndex + len) <= Long.SIZE_BITS

    public fun readerIndex(): Int = readerIndex

    public fun readerIndex(index: Int): UnsafeLongBackedBitBuf {
        readerIndex = index
        return this
    }

    public fun writerIndex(): Int = writerIndex

    public fun writerIndex(index: Int): UnsafeLongBackedBitBuf {
        writerIndex = index
        return this
    }

    public fun clear(): UnsafeLongBackedBitBuf {
        this.readerIndex = 0
        this.writerIndex = 0
        return this
    }

    private companion object {
        private const val LOG_BITS_PER_LONG = 6
        private const val LONG_SIZE_BITS = 1 shl LOG_BITS_PER_LONG
        private const val MASK_BITS_PER_LONG = LONG_SIZE_BITS - 1
    }
}
