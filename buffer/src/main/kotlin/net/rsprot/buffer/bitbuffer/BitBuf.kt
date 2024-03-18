package net.rsprot.buffer.bitbuffer

import com.github.michaelbull.logging.InlineLogger
import io.netty.buffer.ByteBuf
import io.netty.util.internal.SystemPropertyUtil

@Suppress("NOTHING_TO_INLINE", "unused", "MemberVisibilityCanBePrivate")
public class BitBuf(private val buffer: ByteBuf) : AutoCloseable {
    private var writerIndex: Int = buffer.writerIndex() shl LOG_BITS_PER_BYTE
        private set(value) {
            field = value
            buffer.writerIndex(value ushr LOG_BITS_PER_BYTE)
        }
    private var readerIndex: Int = buffer.readerIndex() shl LOG_BITS_PER_BYTE
        private set(value) {
            field = value
            buffer.readerIndex(value ushr LOG_BITS_PER_BYTE)
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
        val index = this.writerIndex
        var bytePos = index ushr LOG_BITS_PER_BYTE
        var bitPos = BYTE_SIZE_BITS - (index and MASK_BITS_PER_BYTE)
        this.writerIndex += count
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
        var bytePos = readerIndex shr LOG_BITS_PER_BYTE
        var bitPos = BYTE_SIZE_BITS - (readerIndex and MASK_BITS_PER_BYTE)
        var value = 0
        this.readerIndex += remaining
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

    private fun ensureWritable(len: Int): BitBuf {
        req(len > 0)

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
        return (readerIndex + count) <= capacity()
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
        if (bitbufferErrorChecking) {
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
        if (bitbufferErrorChecking) {
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
    }

    private companion object {
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
    }
}
