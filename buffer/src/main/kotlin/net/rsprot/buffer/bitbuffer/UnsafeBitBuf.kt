@file:Suppress("DuplicatedCode")

package net.rsprot.buffer.bitbuffer

import io.netty.buffer.ByteBuf
import io.netty.util.internal.shaded.org.jctools.util.UnsafeAccess

@Suppress("NOTHING_TO_INLINE", "unused", "MemberVisibilityCanBePrivate")
public class UnsafeBitBuf(
    buffer: ByteBuf,
) : BitBuf(buffer) {
    private val maxCapacity: Int get() = buffer.maxCapacity()
    private val capacity: Int get() = buffer.capacity()
    private val address: Long = buffer.memoryAddress()

    private inline fun set(
        i: Int,
        value: Int,
    ) {
        unsafe.putByte(address + i, value.toByte())
    }

    private inline fun get(idx: Int): Int {
        return unsafe.getByte(address + idx).toInt()
    }

    override fun pBits(
        count: Int,
        value: Int,
    ) {
        req(count in 1..MAX_BITS) {
            "Number of bits must be in 1..32"
        }
        req(isWritable(count))
        pBitsUnsafe(count, value)
    }

    private fun pBitsUnsafe(
        count: Int,
        value: Int,
    ) {
        var rem = count
        val index = this.writerIndex
        var bytePos = index ushr LOG_BITS_PER_BYTE
        var bitPos = BYTE_SIZE_BITS - (index and MASK_BITS_PER_BYTE)
        this.writerIndex += count
        while (rem > bitPos) {
            val ending = value ushr (rem - bitPos) and bitmask(bitPos)
            val byte = get(bytePos)
            set(bytePos++, byte and bitmask(bitPos).inv() or ending)
            rem -= bitPos
            bitPos = BYTE_SIZE_BITS
        }
        val cur = get(bytePos)
        val mask = bitmask(rem)
        val offset = bitPos - rem
        val sanitized = (cur and (mask shl offset).inv())
        val addition = (value and mask) shl offset
        set(bytePos, sanitized or addition)
    }

    private fun pBitsUnsafe(
        count: Int,
        value: Long,
    ) {
        var rem = count
        val index = this.writerIndex
        var bytePos = index ushr LOG_BITS_PER_BYTE
        var bitPos = BYTE_SIZE_BITS - (index and MASK_BITS_PER_BYTE)
        this.writerIndex += count
        while (rem > bitPos) {
            val ending = (value ushr (rem - bitPos)).toInt() and bitmask(bitPos)
            val byte = get(bytePos)
            set(bytePos++, byte and bitmask(bitPos).inv() or ending)
            rem -= bitPos
            bitPos = BYTE_SIZE_BITS
        }
        val cur = get(bytePos)
        val mask = bitmask(rem)
        val offset = bitPos - rem
        val sanitized = (cur and (mask shl offset).inv())
        val addition = (value.toInt() and mask) shl offset
        set(bytePos, sanitized or addition)
    }

    override fun pBits(src: UnsafeLongBackedBitBuf) {
        val count = src.readableBits()
        req(isWritable(count))
        pBitsUnsafe(count, src.gBitsLong(src.readerIndex(), src.readableBits()))
    }

    override fun gBits(count: Int): Int {
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
            val byte = get(bytePos++) and 0xFF
            value += (byte and bitmask(bitPos)) shl (remaining - bitPos)
            remaining -= bitPos
            bitPos = BYTE_SIZE_BITS
        }
        val bitmask = bitmask(remaining)
        val cur = get(bytePos) and 0xFF
        return value + (cur ushr (bitPos - remaining) and bitmask)
    }

    private companion object {
        private val unsafe = UnsafeAccess.UNSAFE
    }
}
