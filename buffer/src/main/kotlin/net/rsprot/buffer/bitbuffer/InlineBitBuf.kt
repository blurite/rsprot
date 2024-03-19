package net.rsprot.buffer.bitbuffer

import com.github.michaelbull.logging.InlineLogger
import io.netty.buffer.ByteBuf
import io.netty.util.internal.SystemPropertyUtil

@Suppress(
    "NOTHING_TO_INLINE",
    "unused",
    "MemberVisibilityCanBePrivate",
    "DuplicatedCode",
)
@JvmInline
public value class InlineBitBuf private constructor(private val buffer: ByteBuf) {
    private inline fun internalWriterIndex(): Int {
        return buffer.getInt(WRITER_INT_INDEX)
    }

    private inline fun internalWriterIndex(value: Int) {
        buffer.setInt(WRITER_INT_INDEX, value)
    }

    private inline fun internalReaderIndex(): Int {
        return buffer.getInt(READER_INT_INDEX)
    }

    private inline fun internalReaderIndex(value: Int) {
        buffer.setInt(READER_INT_INDEX, value)
    }

    init {
        req(buffer.capacity() <= Int.MAX_VALUE ushr LOG_BITS_PER_BYTE) {
            "This bitbuffer cannot work with buffers greater than 268,435,455 bytes in capacity."
        }
    }

    private inline fun bitmask(pos: Int): Int {
        return (1 shl pos) - 1
    }

    public fun pBits(
        count: Int,
        value: Int,
    ) {
        req(count in 1..MAX_BITS) {
            "Number of bits must be in 1..32"
        }
        if (bitbufferEnsureWritable) {
            ensureWritable(count)
        } else {
            req(isWritable(count))
        }
        var rem = count
        val index = internalWriterIndex()
        var bytePos = index ushr LOG_BITS_PER_BYTE
        var bitPos = BYTE_SIZE_BITS - (index and MASK_BITS_PER_BYTE)
        internalWriterIndex(index + count)
        while (rem > bitPos) {
            val ending = value ushr (rem - bitPos) and bitmask(bitPos)
            val byte = buffer.getByte(bytePos).toInt()
            buffer.setByte(bytePos++, byte and bitmask(bitPos).inv() or ending)
            rem -= bitPos
            bitPos = BYTE_SIZE_BITS
        }
        val cur = buffer.getByte(bytePos).toInt()
        val mask = bitmask(rem)
        val offset = bitPos - rem
        val sanitized = (cur and (mask shl offset).inv())
        val addition = (value and mask) shl offset
        buffer.setByte(bytePos, sanitized or addition)
    }

    public fun gBits(count: Int): Int {
        req(count in 1..MAX_BITS) {
            "Number of bits must be in 1..32"
        }
        req(isReadable(count))
        var remaining = count
        val readerIndex = internalReaderIndex()
        var bytePos = readerIndex shr LOG_BITS_PER_BYTE
        var bitPos = BYTE_SIZE_BITS - (readerIndex and MASK_BITS_PER_BYTE)
        var value = 0
        internalReaderIndex(readerIndex + remaining)
        while (remaining > bitPos) {
            val byte = buffer.getUnsignedByte(bytePos++).toInt()
            value += (byte and bitmask(bitPos)) shl (remaining - bitPos)
            remaining -= bitPos
            bitPos = BYTE_SIZE_BITS
        }
        val bitmask = bitmask(remaining)
        val cur = buffer.getUnsignedByte(bytePos).toInt()
        return value + (cur ushr (bitPos - remaining) and bitmask)
    }

    private fun ensureWritable(len: Int): InlineBitBuf {
        req(len > 0)
        val writerIndex = internalWriterIndex()
        if (bitbufferErrorChecking) {
            if ((writerIndex + len) > maxCapacity()) {
                throw IndexOutOfBoundsException("($writerIndex + $len) > ${maxCapacity()}")
            }
        }

        val currentByteIndex = writerIndex shr LOG_BITS_PER_BYTE
        val nextByteIndex = (writerIndex + len + MASK_BITS_PER_BYTE) shr LOG_BITS_PER_BYTE
        buffer.ensureWritable(nextByteIndex - currentByteIndex)
        req(buffer.capacity() <= Int.MAX_VALUE ushr LOG_BITS_PER_BYTE) {
            "This bitbuffer cannot work with buffers greater than 268,435,455 bytes in capacity."
        }
        return this
    }

    public fun capacity(): Int {
        return buffer.capacity() shl LOG_BITS_PER_BYTE
    }

    public fun capacity(bitCount: Int): InlineBitBuf {
        buffer.capacity((bitCount + MASK_BITS_PER_BYTE) ushr LOG_BITS_PER_BYTE)
        return this
    }

    public fun maxCapacity(): Long {
        return buffer.maxCapacity().toLong() shl LOG_BITS_PER_BYTE
    }

    public fun isReadable(): Boolean {
        return internalReaderIndex() < internalWriterIndex()
    }

    public fun isReadable(count: Int): Boolean {
        req(count >= 0)
        return (internalReaderIndex() + count) <= capacity()
    }

    public fun isWritable(): Boolean {
        return internalWriterIndex() < capacity()
    }

    public fun isWritable(len: Int): Boolean {
        req(len >= 0)
        return (internalWriterIndex() + len) <= capacity()
    }

    public fun readerIndex(): Int {
        return internalReaderIndex()
    }

    public fun readerIndex(index: Int): InlineBitBuf {
        if (bitbufferErrorChecking) {
            if (index < 0 || index > internalWriterIndex()) {
                throw IndexOutOfBoundsException()
            }
        }
        internalReaderIndex(index)
        return this
    }

    public fun writerIndex(): Int {
        return internalWriterIndex()
    }

    public fun writerIndex(index: Int): InlineBitBuf {
        if (bitbufferErrorChecking) {
            if (index < internalReaderIndex() || index > capacity()) {
                throw IndexOutOfBoundsException()
            }
        }
        internalWriterIndex(index)
        return this
    }

    public fun clear(): InlineBitBuf {
        buffer.setInt(WRITER_INT_INDEX, INDEX_PADDING shl LOG_BITS_PER_BYTE)
        buffer.setInt(READER_INT_INDEX, INDEX_PADDING shl LOG_BITS_PER_BYTE)
        buffer.writerIndex(INDEX_PADDING)
        buffer.readerIndex(INDEX_PADDING)
        return this
    }

    public fun close() {
        val writerIndex = internalWriterIndex()
        val bits = (((writerIndex + MASK_BITS_PER_BYTE) and MASK_BITS_PER_BYTE.inv()) - writerIndex)
        if (bits != 0) {
            pBits(bits, 0)
            buffer.writerIndex((writerIndex + bits) ushr LOG_BITS_PER_BYTE)
        } else {
            buffer.writerIndex(writerIndex ushr LOG_BITS_PER_BYTE)
        }
        buffer.readerIndex(
            ((internalReaderIndex() + MASK_BITS_PER_BYTE) and MASK_BITS_PER_BYTE.inv()) ushr LOG_BITS_PER_BYTE,
        )
    }

    public companion object {
        public const val INDEX_PADDING: Int = 8
        private const val WRITER_INT_INDEX = 0
        private const val READER_INT_INDEX = 4
        private const val LOG_BITS_PER_BYTE = 3
        private const val BYTE_SIZE_BITS = 1 shl LOG_BITS_PER_BYTE
        private const val MASK_BITS_PER_BYTE = BYTE_SIZE_BITS - 1
        private const val MAX_BITS = 32
        private val logger = InlineLogger()
        private val bitbufferErrorChecking: Boolean =
            SystemPropertyUtil.getBoolean(
                "net.rsprot.buffer.bitbufferErrorChecking",
                true,
            )
        private val bitbufferEnsureWritable: Boolean =
            SystemPropertyUtil.getBoolean(
                "net.rsprot.buffer.bitbufferEnsureWritable",
                true,
            )

        init {
            logger.debug {
                "-Dnet.rsprot.buffer.bitbufferErrorChecking: $bitbufferErrorChecking"
            }
            logger.debug {
                "-Dnet.rsprot.buffer.bitbufferEnsureWritable: $bitbufferEnsureWritable"
            }
        }

        private inline fun req(value: Boolean) {
            req(value) {
                "Failed requirement."
            }
        }

        private inline fun req(
            value: Boolean,
            crossinline lazyMessage: () -> Any,
        ) {
            if (bitbufferErrorChecking) {
                require(value, lazyMessage)
            }
        }

        /**
         * Wraps the [buffer] with an inline bit buffer.
         * The [buffer] must meet several requirements:
         * 1. It must not have any data in it (reader and writer indexes at 0
         * 2. At least one byte must be writeable to it.
         * 3. The first byte will of the [buffer] will be reserved to storing bit reader and writer indexes.
         *
         * By utilizing the same underlying buffer to store reader and writer indexes,
         * we avoid allocating new bit buffer classes, and ensure memory locality between
         * the underlying buffer and the indices.
         */
        @PublishedApi
        internal fun wrap(buffer: ByteBuf): InlineBitBuf {
            req(!buffer.isReadable)
            req(buffer.writerIndex() == 0)
            req(buffer.isWritable(INDEX_PADDING))
            val inlineBuffer = InlineBitBuf(buffer)
            inlineBuffer.clear()
            return inlineBuffer
        }

        @JvmSynthetic
        public inline fun use(
            buffer: ByteBuf,
            block: InlineBitBuf.() -> Unit,
        ) {
            // Cannot use AutoCloseable interface here as it causes the buffer to be boxed
            val inlineBuffer = wrap(buffer)
            try {
                block(inlineBuffer)
            } finally {
                inlineBuffer.close()
            }
        }
    }
}
