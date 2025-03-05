package net.rsprot.buffer.extensions

import io.netty.buffer.ByteBuf
import io.netty.util.ByteProcessor
import net.rsprot.buffer.JagByteBuf
import net.rsprot.buffer.util.charset.Cp1252Charset
import net.rsprot.crypto.crc.CyclicRedundancyCheck
import java.nio.charset.Charset

private const val HALF_UBYTE = 0x80

/**
 * Reads an unsigned byte from this buffer.
 */
public fun ByteBuf.g1(): Int {
    return readUnsignedByte().toInt()
}

/**
 * Reads a signed byte from this buffer.
 */
public fun ByteBuf.g1s(): Int {
    return readByte().toInt()
}

/**
 * Writes a byte with the value of [value] to this buffer.
 * The 24 high-order bits of the specified [value] are ignored.
 */
public fun ByteBuf.p1(value: Int): ByteBuf {
    return writeByte(value)
}

/**
 * Reads an unsigned byte from this buffer with the `value - 128` byte modification.
 */
public fun ByteBuf.g1Alt1(): Int {
    return (readUnsignedByte() - HALF_UBYTE) and 0xFF
}

/**
 * Reads a signed byte from this buffer with the `value - 128` byte modification.
 */
public fun ByteBuf.g1sAlt1(): Int {
    return (readUnsignedByte() - HALF_UBYTE).toByte().toInt()
}

/**
 * Writes a byte with the value of [value] to this buffer, using the `value - 128` byte modification.
 * The 24 high-order bits of the specified [value] are ignored.
 */
public fun ByteBuf.p1Alt1(value: Int): ByteBuf {
    return writeByte(value + HALF_UBYTE)
}

/**
 * Reads an unsigned byte from this buffer with the `0 - value` byte modification.
 */
public fun ByteBuf.g1Alt2(): Int {
    return (-readByte().toInt() and 0xFF)
}

/**
 * Reads a signed byte from this buffer with the `0 - value` byte modification.
 */
public fun ByteBuf.g1sAlt2(): Int {
    return -readByte().toInt()
}

/**
 * Writes a byte with the value of [value] to this buffer, using the `0 - value` byte modification.
 * The 24 high-order bits of the specified [value] are ignored.
 */
public fun ByteBuf.p1Alt2(value: Int): ByteBuf {
    return writeByte(-value)
}

/**
 * Reads an unsigned byte from this buffer with the `128 - value` byte modification.
 */
public fun ByteBuf.g1Alt3(): Int {
    return (HALF_UBYTE - readByte().toInt()) and 0xFF
}

/**
 * Reads a signed byte from this buffer with the `128 - value` byte modification.
 */
public fun ByteBuf.g1sAlt3(): Int {
    return (HALF_UBYTE - readByte()).toByte().toInt()
}

/**
 * Writes a byte with the value of [value] to this buffer, using the `128 - value` byte modification.
 * The 24 high-order bits of the specified [value] are ignored.
 */
public fun ByteBuf.p1Alt3(value: Int): ByteBuf {
    return writeByte(HALF_UBYTE - value)
}

/**
 * Reads an unsigned short from this buffer.
 */
public fun ByteBuf.g2(): Int {
    return readUnsignedShort()
}

/**
 * Reads a signed short from this buffer.
 */
public fun ByteBuf.g2s(): Int {
    return readShort().toInt()
}

/**
 * Writes a short with the value of [value] to this buffer.
 * The 16 high-order bits of the specified [value] are ignored.
 */
public fun ByteBuf.p2(value: Int): ByteBuf {
    return writeShort(value)
}

/**
 * Reads an unsigned short with the `LITTLE_ENDIAN` order from this buffer.
 */
public fun ByteBuf.g2Alt1(): Int {
    return readUnsignedShortLE()
}

/**
 * Reads a signed short with the `LITTLE_ENDIAN` order from this buffer.
 */
public fun ByteBuf.g2sAlt1(): Int {
    return readShortLE().toInt()
}

/**
 * Writes a short with the value of [value] to this buffer in the `LITTLE_ENDIAN` order.
 * The 16 high-order bits of the specified [value] are ignored.
 */
public fun ByteBuf.p2Alt1(value: Int): ByteBuf {
    writeByte(value)
    writeByte(value shr 8)
    return this
}

/**
 * Reads an unsigned short from this buffer.
 * The higher order bits of this short are modified with the `value - 128` byte modification.
 */
public fun ByteBuf.g2Alt2(): Int {
    val short = readUnsignedShort()
    return (short and 0xFF00)
        .or(((short and 0xFF) - HALF_UBYTE) and 0xFF)
}

/**
 * Reads a signed short from this buffer.
 * The higher order bits of this short are modified with the `value - 128` byte modification.
 */
public fun ByteBuf.g2sAlt2(): Int {
    val short = readUnsignedShort()
    val value =
        (short and 0xFF00)
            .or(((short and 0xFF) - HALF_UBYTE) and 0xFF)
    return if (value > 0x7FFF) {
        value - 0x10000
    } else {
        value
    }
}

/**
 * Writes a short with the value of [value] to this buffer with the `value + 128`
 * byte modification on the higher order bits of this short.
 * The 16 high-order bits of the specified [value] are ignored.
 */
public fun ByteBuf.p2Alt2(value: Int): ByteBuf {
    writeByte(value shr 8)
    writeByte(value + HALF_UBYTE)
    return this
}

/**
 * Reads an unsigned short from this buffer in the `LITTLE_ENDIAN` order.
 * The lower order bits of this short are modified with the `value - 128` byte modification.
 */
public fun ByteBuf.g2Alt3(): Int {
    val short = readUnsignedShort()
    return (((short ushr 8) - HALF_UBYTE) and 0xFF)
        .or(short and 0xFF shl 8)
}

/**
 * Reads a signed short from this buffer in the `LITTLE_ENDIAN` order.
 * The lower order bits of this short are modified with the `value - 128` byte modification.
 */
public fun ByteBuf.g2sAlt3(): Int {
    val short = readUnsignedShort()
    val value =
        (((short ushr 8) - HALF_UBYTE) and 0xFF)
            .or(short and 0xFF shl 8)
    return if (value > 0x7FFF) {
        value - 0x10000
    } else {
        value
    }
}

/**
 * Writes a short with the value of [value] to this buffer with the `value + 128`
 * byte modification on the lower order bits of this short.
 * The 16 high-order bits of the specified [value] are ignored.
 */
public fun ByteBuf.p2Alt3(value: Int): ByteBuf {
    writeByte(value + HALF_UBYTE)
    writeByte(value shr 8)
    return this
}

/**
 * Reads a 24-bit unsigned medium integer from this buffer.
 */
public fun ByteBuf.g3(): Int {
    return readUnsignedMedium()
}

/**
 * Reads a 24-bit signed medium integer from this buffer.
 */
public fun ByteBuf.g3s(): Int {
    return readMedium()
}

/**
 * Writes a 24-bit medium integer with the value of [value] to this buffer.
 * The 8 high-order bits of the specified [value] are ignored.
 */
public fun ByteBuf.p3(value: Int): ByteBuf {
    return writeMedium(value)
}

/**
 * Reads a 24-bit unsigned medium integer with the `LITTLE_ENDIAN` order from this buffer.
 */
public fun ByteBuf.g3Alt1(): Int {
    return readUnsignedMediumLE()
}

/**
 * Reads a 24-bit signed medium integer with the `LITTLE_ENDIAN` order from this buffer.
 */
public fun ByteBuf.g3sAlt1(): Int {
    return readMediumLE()
}

/**
 * Writes a 24-bit medium integer with the value of [value] using `LITTLE_ENDIAN` order to this buffer.
 * The 8 high-order bits of the specified [value] are ignored.
 */
public fun ByteBuf.p3Alt1(value: Int): ByteBuf {
    return writeMediumLE(value)
}

public fun ByteBuf.g3Alt2(): Int {
    val medium = readUnsignedMedium()
    return (medium and 0xFF shl 8)
        .or(medium ushr 8 and 0xFF)
        .or(medium ushr 16 and 0xFF shl 16)
}

public fun ByteBuf.g3sAlt2(): Int {
    val medium = readUnsignedMedium()
    val result =
        (medium and 0xFF shl 8)
            .or(medium ushr 8 and 0xFF)
            .or(medium ushr 16 and 0xFF shl 16)
    return if (result > 0x7FFFFF) {
        result - 0x1000000
    } else {
        result
    }
}

public fun ByteBuf.p3Alt2(value: Int): ByteBuf {
    writeByte(value shr 16)
    writeByte(value)
    writeByte(value shr 8)
    return this
}

public fun ByteBuf.g3Alt3(): Int {
    val medium = readUnsignedMedium()
    return (medium and 0xFF)
        .or(medium ushr 8 and 0xFF shl 16)
        .or(medium ushr 16 and 0xFF shl 8)
}

public fun ByteBuf.g3sAlt3(): Int {
    val medium = readUnsignedMedium()
    val result =
        (medium and 0xFF)
            .or(medium ushr 8 and 0xFF shl 16)
            .or(medium ushr 16 and 0xFF shl 8)
    return if (result > 0x7FFFFF) {
        result - 0x1000000
    } else {
        result
    }
}

public fun ByteBuf.p3Alt3(value: Int): ByteBuf {
    writeByte(value shr 8)
    writeByte(value shr 16)
    writeByte(value)
    return this
}

public fun ByteBuf.g4(): Int {
    return readInt()
}

public fun ByteBuf.p4(value: Int): ByteBuf {
    return writeInt(value)
}

public fun ByteBuf.g4Alt1(): Int {
    return readIntLE()
}

public fun ByteBuf.p4Alt1(value: Int): ByteBuf {
    return writeIntLE(value)
}

public fun ByteBuf.g4Alt2(): Int {
    var value = 0
    value = value or (g1() shl 8)
    value = value or g1()
    value = value or (g1() shl 24)
    value = value or (g1() shl 16)
    return value
}

public fun ByteBuf.p4Alt2(value: Int): ByteBuf {
    writeByte(value shr 8)
    writeByte(value)
    writeByte(value shr 24)
    writeByte(value shr 16)
    return this
}

public fun ByteBuf.g4Alt3(): Int {
    var value = 0
    value = value or (g1() shl 16)
    value = value or (g1() shl 24)
    value = value or g1()
    value = value or (g1() shl 8)
    return value
}

public fun ByteBuf.p4Alt3(value: Int): ByteBuf {
    writeByte(value shr 16)
    writeByte(value shr 24)
    writeByte(value)
    writeByte(value shr 8)
    return this
}

public fun ByteBuf.g8(): Long {
    return readLong()
}

public fun ByteBuf.p8(value: Long): ByteBuf {
    return writeLong(value)
}

public fun ByteBuf.g4f(): Float {
    return readFloat()
}

public fun ByteBuf.p4f(value: Float): ByteBuf {
    return writeFloat(value)
}

public fun ByteBuf.g8d(): Double {
    return readDouble()
}

public fun ByteBuf.p8d(value: Double): ByteBuf {
    return writeDouble(value)
}

public fun ByteBuf.gboolean(): Boolean {
    return g1() and 0x1 != 0
}

public fun ByteBuf.pboolean(value: Boolean): ByteBuf {
    return writeByte(if (value) 0x1 else 0)
}

public fun ByteBuf.gjstrnull(): String? {
    if (getByte(readerIndex()).toInt() == 0) {
        readerIndex(readerIndex() + 1)
        return null
    }
    return gjstr()
}

public fun ByteBuf.gjstr(): String {
    return readString()
}

public fun ByteBuf.pjstr(
    s: CharSequence,
    charset: Charset = Cp1252Charset,
): ByteBuf {
    writeCharSequence(s, charset)
    writeByte(0)
    return this
}

public fun ByteBuf.pjstrnull(
    s: CharSequence?,
    charset: Charset = Cp1252Charset,
): ByteBuf {
    if (s != null) {
        writeCharSequence(s, charset)
    }
    writeByte(0)
    return this
}

public fun ByteBuf.gjstr2(): String {
    if (readByte().toInt() != 0) {
        throw IllegalStateException("Expected byte to be 0 in position 0")
    }
    return gjstr()
}

public fun ByteBuf.pjstr2(s: CharSequence): ByteBuf {
    writeByte(0)
    writeString(s)
    return this
}

private fun ByteBuf.writeString(
    s: CharSequence,
    charset: Charset = Cp1252Charset,
): ByteBuf {
    writeCharSequence(s, charset)
    writeByte(0)
    return this
}

private fun ByteBuf.readString(charset: Charset = Cp1252Charset): String {
    val start = readerIndex()

    val end = forEachByte(ByteProcessor.FIND_NUL)
    require(end != -1) {
        "Unterminated string"
    }

    val s = toString(start, end - start, charset)
    readerIndex(end + 1)
    return s
}

public fun ByteBuf.gSmart1or2s(): Int {
    val peek = getUnsignedByte(readerIndex()).toInt()
    return if (peek < HALF_UBYTE) {
        g1() - 64
    } else {
        g2() - 49152
    }
}

public fun ByteBuf.pSmart1or2s(value: Int): ByteBuf {
    when (value) {
        in -0x40..0x3F -> writeByte(value + 0x40)
        in -0x4000..0x3FFF -> writeShort(0x8000 or (value + 0x4000))
        else -> throw IllegalArgumentException()
    }
    return this
}

public fun ByteBuf.gSmart1or2(): Int {
    val peek = getUnsignedByte(readerIndex()).toInt()
    return if (peek < HALF_UBYTE) {
        g1()
    } else {
        g2() - 32768
    }
}

public fun ByteBuf.pSmart1or2(value: Int): ByteBuf {
    when (value) {
        in 0..0x7F -> writeByte(value)
        in 0..0x7FFF -> writeShort(0x8000 or value)
        else -> throw IllegalArgumentException()
    }
    return this
}

public fun ByteBuf.gSmart1or2null(): Int {
    val peek = getUnsignedByte(readerIndex()).toInt()
    return if (peek < HALF_UBYTE) {
        g1() - 1
    } else {
        g2() - 32769
    }
}

public fun ByteBuf.pSmart1or2null(value: Int): ByteBuf {
    when (value) {
        in -1..<0x7F -> writeByte(value + 1)
        in -1..<0x7FFF -> writeShort(0x8000 or (value + 1))
        else -> throw IllegalArgumentException()
    }
    return this
}

public fun ByteBuf.gSmart1or2extended(): Int {
    var total = 0
    var num = gSmart1or2()
    while (num == 0x7FFF) {
        total += 0x7FFF
        num = gSmart1or2()
    }
    total += num
    return total
}

public fun ByteBuf.pSmart1or2extended(value: Int): ByteBuf {
    var remaining = value
    while (remaining >= 0x7FFF) {
        pSmart1or2(0x7FFF)
        remaining -= 0x7FFF
    }
    pSmart1or2(remaining)
    return this
}

public fun ByteBuf.gSmart2or4(): Int {
    val peek = getByte(readerIndex()).toInt()
    return if (peek < 0) {
        g4() and Int.MAX_VALUE
    } else {
        g2()
    }
}

public fun ByteBuf.pSmart2or4(value: Int): ByteBuf {
    when (value) {
        in 0..0x7FFF -> writeShort(value)
        in 0..0x7FFFFFFF -> writeInt(0x80000000.toInt() or value)
        else -> throw IllegalArgumentException()
    }
    return this
}

public fun ByteBuf.gSmart2or4null(): Int {
    val peek = getByte(readerIndex()).toInt()
    return if (peek < 0) {
        g4() and Int.MAX_VALUE
    } else {
        val num = g2()
        if (num == 32767) -1 else num
    }
}

public fun ByteBuf.pSmart2or4null(value: Int): ByteBuf {
    when {
        value !in -1..<Int.MAX_VALUE -> throw IllegalArgumentException()
        value == -1 -> {
            writeShort(0x7FFF)
        }
        value < Short.MAX_VALUE -> {
            writeShort(value)
        }
        else -> {
            writeInt(value)
            val writtenValue = getByte(writerIndex() - 4)
            setByte(writerIndex() - 4, writtenValue + HALF_UBYTE)
        }
    }
    return this
}

public fun ByteBuf.gVarInt(): Int {
    var value = 0

    var byte: Int
    do {
        byte = readUnsignedByte().toInt()
        value = (value shl 7) or (byte and 0x7F)
    } while ((byte and HALF_UBYTE) != 0)

    return value
}

public fun ByteBuf.pVarInt(v: Int): ByteBuf {
    if ((v and 0x7F.inv()) != 0) {
        if ((v and 0x3FFF.inv()) != 0) {
            if ((v and 0x1FFFFF.inv()) != 0) {
                if ((v and 0xFFFFFFF.inv()) != 0) {
                    writeByte(((v ushr 28) and 0x7F) or HALF_UBYTE)
                }
                writeByte(((v ushr 21) and 0x7F) or HALF_UBYTE)
            }
            writeByte(((v ushr 14) and 0x7F) or HALF_UBYTE)
        }
        writeByte(((v ushr 7) and 0x7F) or HALF_UBYTE)
    }
    writeByte(v and 0x7F)
    return this
}

public fun ByteBuf.gdata(
    dest: ByteArray,
    offset: Int = 0,
    length: Int = dest.size,
) {
    for (i in offset..<(offset + length)) {
        dest[i] = readByte()
    }
}

public fun ByteBuf.gdata(
    dest: ByteBuf,
    offset: Int = readerIndex(),
    length: Int = readableBytes(),
) {
    dest.writeBytes(this, offset, length)
}

public fun ByteBuf.pdata(
    src: ByteArray,
    offset: Int = 0,
    length: Int = src.size,
): ByteBuf {
    for (i in offset..<(length + offset)) {
        writeByte(src[i].toInt())
    }
    return this
}

public fun ByteBuf.pdata(
    src: ByteBuf,
    offset: Int = src.readerIndex(),
    length: Int = src.readableBytes(),
): ByteBuf {
    for (i in offset..<(length + offset)) {
        writeByte(src.getByte(i).toInt())
    }
    return this
}

public fun ByteBuf.gdataAlt1(
    dest: ByteArray,
    offset: Int = 0,
    length: Int = dest.size,
) {
    for (i in (offset + length - 1) downTo offset) {
        dest[i] = readByte()
    }
}

public fun ByteBuf.gdataAlt1(
    dest: ByteBuf,
    offset: Int = readerIndex(),
    length: Int = readableBytes(),
) {
    for (i in (offset + length - 1) downTo offset) {
        dest.writeByte(readByte().toInt())
    }
}

public fun ByteBuf.pdataAlt1(
    data: ByteArray,
    offset: Int = 0,
    length: Int = data.size,
): ByteBuf {
    for (i in (offset + length - 1) downTo offset) {
        writeByte(data[i].toInt())
    }
    return this
}

public fun ByteBuf.pdataAlt1(
    data: ByteBuf,
    offset: Int = data.readerIndex(),
    length: Int = data.readableBytes(),
): ByteBuf {
    for (i in (offset + length - 1) downTo offset) {
        writeByte(data.getByte(i).toInt())
    }
    return this
}

public fun ByteBuf.gdataAlt2(
    dest: ByteArray,
    offset: Int = 0,
    length: Int = dest.size,
) {
    for (i in offset..<(offset + length)) {
        dest[i] = (readUnsignedByte() - HALF_UBYTE).toByte()
    }
}

public fun ByteBuf.gdataAlt2(
    dest: ByteBuf,
    offset: Int = readerIndex(),
    length: Int = readableBytes(),
) {
    for (i in offset..<(offset + length)) {
        dest.writeByte(readUnsignedByte() - HALF_UBYTE)
    }
}

public fun ByteBuf.pdataAlt2(
    data: ByteArray,
    offset: Int = 0,
    length: Int = data.size,
): ByteBuf {
    for (i in offset..<(offset + length)) {
        writeByte(data[i].toInt() + HALF_UBYTE)
    }
    return this
}

public fun ByteBuf.pdataAlt2(
    data: ByteBuf,
    offset: Int = data.readerIndex(),
    length: Int = data.readableBytes(),
): ByteBuf {
    for (i in offset..<(offset + length)) {
        writeByte(data.getByte(i) + HALF_UBYTE)
    }
    return this
}

public fun ByteBuf.gdataAlt3(
    dest: ByteArray,
    offset: Int = 0,
    length: Int = dest.size,
) {
    for (i in (offset + length - 1) downTo offset) {
        dest[i] = (readUnsignedByte() - HALF_UBYTE).toByte()
    }
}

public fun ByteBuf.gdataAlt3(
    dest: ByteBuf,
    offset: Int = readerIndex(),
    length: Int = readableBytes(),
) {
    for (i in (offset + length - 1) downTo offset) {
        dest.writeByte(readUnsignedByte() - HALF_UBYTE)
    }
}

public fun ByteBuf.pdataAlt3(
    data: ByteArray,
    offset: Int = 0,
    length: Int = data.size,
): ByteBuf {
    for (i in (offset + length - 1) downTo offset) {
        writeByte(data[i].toInt() + HALF_UBYTE)
    }
    return this
}

public fun ByteBuf.pdataAlt3(
    data: ByteBuf,
    offset: Int = data.readerIndex(),
    length: Int = data.readableBytes(),
): ByteBuf {
    for (i in (offset + length - 1) downTo offset) {
        writeByte(data.getByte(i) + HALF_UBYTE)
    }
    return this
}

public fun ByteBuf.toJagByteBuf(): JagByteBuf {
    return JagByteBuf(this)
}

/**
 * Adds a 32-bit checksum of the buffer's slice, computed by
 * starting from [startIndex] and ending at [io.netty.buffer.ByteBuf.writerIndex].
 * @param startIndex the writer index to begin computing the checksum from
 * @return the checksum of this buffer's slice.
 */
public fun ByteBuf.addCRC32(startIndex: Int): Int {
    val checksum = CyclicRedundancyCheck.computeCrc32(this, startIndex, writerIndex())
    p4(checksum)
    return checksum
}

/**
 * Checks whether the checksum written with the payload matches the checksum
 * of the payload itself.
 * Note that the checksum is computed starting at the readerIndex = 0.
 * The readerIndex is expected to be at the end of the crc32 value itself.
 * @return whether the written crc32 matches with the crc32 that we computed.
 */
public fun ByteBuf.checkCRC32(): Boolean {
    val length = readerIndex()
    readerIndex(length - 4)
    val checksum = CyclicRedundancyCheck.computeCrc32(this, 0, length - 4)
    return checksum == g4()
}
