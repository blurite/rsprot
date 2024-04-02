@file:Suppress("NOTHING_TO_INLINE")

package net.rsprot.buffer.bitbuffer

import com.github.michaelbull.logging.InlineLogger
import io.netty.buffer.ByteBuf
import io.netty.util.internal.SystemPropertyUtil

public abstract class BitBuf(
    protected val buffer: ByteBuf,
) : AutoCloseable {
    protected var writerIndex: Int = buffer.writerIndex() shl LOG_BITS_PER_BYTE
    protected var readerIndex: Int = buffer.readerIndex() shl LOG_BITS_PER_BYTE

    protected inline fun bitmask(pos: Int): Int {
        return (1 shl pos) - 1
    }

    public abstract fun pBits(
        count: Int,
        value: Int,
    )

    public abstract fun pBits(src: UnsafeLongBackedBitBuf)

    public abstract fun gBits(count: Int): Int

    public fun capacity(): Int {
        return buffer.capacity() shl LOG_BITS_PER_BYTE
    }

    public fun capacity(bitCount: Int): BitBuf {
        buffer.capacity((bitCount + MASK_BITS_PER_BYTE) ushr LOG_BITS_PER_BYTE)
        return this
    }

    public fun maxCapacity(): Long {
        return buffer.maxCapacity().toLong() shl LOG_BITS_PER_BYTE
    }

    public fun isReadable(): Boolean {
        return readerIndex < writerIndex
    }

    public fun isReadable(count: Int): Boolean {
        req(count >= 0)
        return (readerIndex + count) <= writerIndex
    }

    public fun readableBits(): Int {
        return writerIndex - readerIndex
    }

    public fun isWritable(): Boolean {
        return writerIndex < capacity()
    }

    public fun isWritable(len: Int): Boolean {
        req(len >= 0)
        return (writerIndex + len) <= capacity()
    }

    public fun readerIndex(): Int {
        return readerIndex
    }

    public fun readerIndex(index: Int): BitBuf {
        if (errorChecking) {
            if (index < 0 || index > writerIndex) {
                throw IndexOutOfBoundsException()
            }
        }
        readerIndex = index
        return this
    }

    public fun writerIndex(): Int {
        return writerIndex
    }

    public fun writerIndex(index: Int): BitBuf {
        if (errorChecking) {
            if (index < readerIndex || index > capacity()) {
                throw IndexOutOfBoundsException()
            }
        }
        writerIndex = index
        return this
    }

    public fun clear(): BitBuf {
        this.readerIndex = 0
        this.writerIndex = 0
        return this
    }

    override fun close() {
        val bits = (((writerIndex + MASK_BITS_PER_BYTE) and MASK_BITS_PER_BYTE.inv()) - writerIndex)
        if (bits != 0) {
            pBits(bits, 0)
        }
        readerIndex = (readerIndex + MASK_BITS_PER_BYTE) and MASK_BITS_PER_BYTE.inv()
        buffer.writerIndex(writerIndex ushr LOG_BITS_PER_BYTE)
        buffer.readerIndex(readerIndex ushr LOG_BITS_PER_BYTE)
    }

    public companion object {
        public const val LOG_BITS_PER_BYTE: Int = 3
        public const val BYTE_SIZE_BITS: Int = 1 shl LOG_BITS_PER_BYTE
        public const val MASK_BITS_PER_BYTE: Int = BYTE_SIZE_BITS - 1
        public const val MAX_BITS: Int = 32
        private val logger = InlineLogger()
        public val errorChecking: Boolean =
            SystemPropertyUtil.getBoolean(
                "net.rsprot.buffer.bitbufferErrorChecking",
                true,
            )

        init {
            logger.debug {
                "-Dnet.rsprot.buffer.bitbufferErrorChecking: $errorChecking"
            }
        }

        @JvmStatic
        protected inline fun req(value: Boolean) {
            req(value) {
                "Failed requirement."
            }
        }

        @JvmStatic
        protected inline fun req(
            value: Boolean,
            crossinline lazyMessage: () -> Any,
        ) {
            if (errorChecking) {
                require(value, lazyMessage)
            }
        }
    }
}

public fun ByteBuf.toBitBuf(): BitBuf {
    return if (hasMemoryAddress()) {
        UnsafeBitBuf(this)
    } else {
        WrappedBitBuf(this)
    }
}
