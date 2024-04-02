@file:Suppress("DuplicatedCode")

package net.rsprot.buffer.bitbuffer

import io.netty.buffer.ByteBuf

@Suppress("unused", "MemberVisibilityCanBePrivate")
public class WrappedBitBuf(buffer: ByteBuf) : BitBuf(buffer) {
    init {
        req(buffer.capacity() <= Int.MAX_VALUE ushr LOG_BITS_PER_BYTE) {
            "This bitbuffer cannot work with buffers greater than 268,435,455 bytes in capacity."
        }
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
            val byte = buffer.getByte(bytePos).toInt()
            buffer.setByte(bytePos++, byte and bitmask(bitPos).inv() or ending)
            rem -= bitPos
            bitPos = BYTE_SIZE_BITS
        }
        val cur = buffer.getByte(bytePos).toInt()
        val mask = bitmask(rem)
        val offset = bitPos - rem
        val sanitized = (cur and (mask shl offset).inv())
        val addition = (value.toInt() and mask) shl offset
        buffer.setByte(bytePos, sanitized or addition)
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
            val byte = buffer.getUnsignedByte(bytePos++).toInt()
            value += (byte and bitmask(bitPos)) shl (remaining - bitPos)
            remaining -= bitPos
            bitPos = BYTE_SIZE_BITS
        }
        val bitmask = bitmask(remaining)
        val cur = buffer.getUnsignedByte(bytePos).toInt()
        return value + (cur ushr (bitPos - remaining) and bitmask)
    }
}
