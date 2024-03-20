package net.rsprot.buffer

import io.netty.buffer.ByteBuf
import net.rsprot.buffer.extensions.fastgjstring
import net.rsprot.buffer.extensions.g1
import net.rsprot.buffer.extensions.g1Alt1
import net.rsprot.buffer.extensions.g1Alt2
import net.rsprot.buffer.extensions.g1Alt3
import net.rsprot.buffer.extensions.g1s
import net.rsprot.buffer.extensions.g1sAlt1
import net.rsprot.buffer.extensions.g1sAlt2
import net.rsprot.buffer.extensions.g1sAlt3
import net.rsprot.buffer.extensions.g2
import net.rsprot.buffer.extensions.g2Alt1
import net.rsprot.buffer.extensions.g2Alt2
import net.rsprot.buffer.extensions.g2Alt3
import net.rsprot.buffer.extensions.g2s
import net.rsprot.buffer.extensions.g2sAlt1
import net.rsprot.buffer.extensions.g2sAlt2
import net.rsprot.buffer.extensions.g2sAlt3
import net.rsprot.buffer.extensions.g3
import net.rsprot.buffer.extensions.g3Alt1
import net.rsprot.buffer.extensions.g3Alt2
import net.rsprot.buffer.extensions.g3Alt3
import net.rsprot.buffer.extensions.g3s
import net.rsprot.buffer.extensions.g3sAlt1
import net.rsprot.buffer.extensions.g3sAlt2
import net.rsprot.buffer.extensions.g3sAlt3
import net.rsprot.buffer.extensions.g4
import net.rsprot.buffer.extensions.g4Alt1
import net.rsprot.buffer.extensions.g4Alt2
import net.rsprot.buffer.extensions.g4Alt3
import net.rsprot.buffer.extensions.g4f
import net.rsprot.buffer.extensions.g8
import net.rsprot.buffer.extensions.g8d
import net.rsprot.buffer.extensions.gSmart1or2
import net.rsprot.buffer.extensions.gSmart1or2extended
import net.rsprot.buffer.extensions.gSmart1or2null
import net.rsprot.buffer.extensions.gSmart1or2s
import net.rsprot.buffer.extensions.gSmart2or4
import net.rsprot.buffer.extensions.gSmart2or4null
import net.rsprot.buffer.extensions.gVarInt
import net.rsprot.buffer.extensions.gboolean
import net.rsprot.buffer.extensions.gdata
import net.rsprot.buffer.extensions.gdataAlt1
import net.rsprot.buffer.extensions.gdataAlt2
import net.rsprot.buffer.extensions.gjstr
import net.rsprot.buffer.extensions.gjstr2
import net.rsprot.buffer.extensions.p1
import net.rsprot.buffer.extensions.p1Alt1
import net.rsprot.buffer.extensions.p1Alt2
import net.rsprot.buffer.extensions.p1Alt3
import net.rsprot.buffer.extensions.p2
import net.rsprot.buffer.extensions.p2Alt1
import net.rsprot.buffer.extensions.p2Alt2
import net.rsprot.buffer.extensions.p2Alt3
import net.rsprot.buffer.extensions.p3
import net.rsprot.buffer.extensions.p3Alt1
import net.rsprot.buffer.extensions.p3Alt2
import net.rsprot.buffer.extensions.p3Alt3
import net.rsprot.buffer.extensions.p4
import net.rsprot.buffer.extensions.p4Alt1
import net.rsprot.buffer.extensions.p4Alt2
import net.rsprot.buffer.extensions.p4Alt3
import net.rsprot.buffer.extensions.p4f
import net.rsprot.buffer.extensions.p8
import net.rsprot.buffer.extensions.p8d
import net.rsprot.buffer.extensions.pSmart1or2
import net.rsprot.buffer.extensions.pSmart1or2extended
import net.rsprot.buffer.extensions.pSmart1or2null
import net.rsprot.buffer.extensions.pSmart1or2s
import net.rsprot.buffer.extensions.pSmart2or4
import net.rsprot.buffer.extensions.pSmart2or4null
import net.rsprot.buffer.extensions.pVarInt
import net.rsprot.buffer.extensions.pboolean
import net.rsprot.buffer.extensions.pdata
import net.rsprot.buffer.extensions.pdataAlt1
import net.rsprot.buffer.extensions.pdataAlt2
import net.rsprot.buffer.extensions.pjstr
import net.rsprot.buffer.extensions.pjstr2
import net.rsprot.buffer.util.charset.Cp1252Charset
import java.nio.charset.Charset

/**
 * A [ByteBuf] wrapper that supplies all the basic RS protocol functions.
 * The wrapped [buffer] will remain accessible in case more control is needed.
 *
 * This is intended as a mechanism to avoid dealing with extension imports,
 * and to hide away non-rs implementations from most of the packet encoders and decoders.
 */
@Suppress("NOTHING_TO_INLINE")
@JvmInline
public value class JagByteBuf(public val buffer: ByteBuf) {
    public inline val isReadable: Boolean
        get() = buffer.isReadable

    public inline val isWritable: Boolean
        get() = buffer.isWritable

    public inline fun isReadable(size: Int): Boolean {
        return buffer.isReadable(size)
    }

    public inline fun isWritable(size: Int): Boolean {
        return buffer.isWritable(size)
    }

    public inline fun readableBytes(): Int {
        return buffer.readableBytes()
    }

    public inline fun writableBytes(): Int {
        return buffer.writableBytes()
    }

    public inline fun skip(num: Int): JagByteBuf {
        buffer.skipBytes(num)
        return this
    }

    public inline fun g1(): Int {
        return buffer.g1()
    }

    public inline fun g1s(): Int {
        return buffer.g1s()
    }

    public inline fun p1(value: Int): JagByteBuf {
        buffer.p1(value)
        return this
    }

    public inline fun g1Alt1(): Int {
        return buffer.g1Alt1()
    }

    public inline fun g1sAlt1(): Int {
        return buffer.g1sAlt1()
    }

    public inline fun p1Alt1(value: Int): JagByteBuf {
        buffer.p1Alt1(value)
        return this
    }

    public inline fun g1Alt2(): Int {
        return buffer.g1Alt2()
    }

    public inline fun g1sAlt2(): Int {
        return buffer.g1sAlt2()
    }

    public inline fun p1Alt2(value: Int): JagByteBuf {
        buffer.p1Alt2(value)
        return this
    }

    public inline fun g1Alt3(): Int {
        return buffer.g1Alt3()
    }

    public inline fun g1sAlt3(): Int {
        return buffer.g1sAlt3()
    }

    public inline fun p1Alt3(value: Int): JagByteBuf {
        buffer.p1Alt3(value)
        return this
    }

    public inline fun g2(): Int {
        return buffer.g2()
    }

    public inline fun g2s(): Int {
        return buffer.g2s()
    }

    public inline fun p2(value: Int): JagByteBuf {
        buffer.p2(value)
        return this
    }

    public inline fun g2Alt1(): Int {
        return buffer.g2Alt1()
    }

    public inline fun g2sAlt1(): Int {
        return buffer.g2sAlt1()
    }

    public inline fun p2Alt1(value: Int): JagByteBuf {
        buffer.p2Alt1(value)
        return this
    }

    public inline fun g2Alt2(): Int {
        return buffer.g2Alt2()
    }

    public inline fun g2sAlt2(): Int {
        return buffer.g2sAlt2()
    }

    public inline fun p2Alt2(value: Int): JagByteBuf {
        buffer.p2Alt2(value)
        return this
    }

    public inline fun g2Alt3(): Int {
        return buffer.g2Alt3()
    }

    public inline fun g2sAlt3(): Int {
        return buffer.g2sAlt3()
    }

    public inline fun p2Alt3(value: Int): JagByteBuf {
        buffer.p2Alt3(value)
        return this
    }

    public inline fun g3(): Int {
        return buffer.g3()
    }

    public inline fun g3s(): Int {
        return buffer.g3s()
    }

    public inline fun p3(value: Int): JagByteBuf {
        buffer.p3(value)
        return this
    }

    public inline fun g3Alt1(): Int {
        return buffer.g3Alt1()
    }

    public inline fun g3sAlt1(): Int {
        return buffer.g3sAlt1()
    }

    public inline fun p3Alt1(value: Int): JagByteBuf {
        buffer.p3Alt1(value)
        return this
    }

    public inline fun g3Alt2(): Int {
        return buffer.g3Alt2()
    }

    public inline fun g3sAlt2(): Int {
        return buffer.g3sAlt2()
    }

    public inline fun p3Alt2(value: Int): JagByteBuf {
        buffer.p3Alt2(value)
        return this
    }

    public inline fun g3Alt3(): Int {
        return buffer.g3Alt3()
    }

    public inline fun g3sAlt3(): Int {
        return buffer.g3sAlt3()
    }

    public inline fun p3Alt3(value: Int): JagByteBuf {
        buffer.p3Alt3(value)
        return this
    }

    public inline fun g4(): Int {
        return buffer.g4()
    }

    public inline fun p4(value: Int): JagByteBuf {
        buffer.p4(value)
        return this
    }

    public inline fun g4Alt1(): Int {
        return buffer.g4Alt1()
    }

    public inline fun p4Alt1(value: Int): JagByteBuf {
        buffer.p4Alt1(value)
        return this
    }

    public inline fun g4Alt2(): Int {
        return buffer.g4Alt2()
    }

    public inline fun p4Alt2(value: Int): JagByteBuf {
        buffer.p4Alt2(value)
        return this
    }

    public inline fun g4Alt3(): Int {
        return buffer.g4Alt3()
    }

    public inline fun p4Alt3(value: Int): JagByteBuf {
        buffer.p4Alt3(value)
        return this
    }

    public inline fun g8(): Long {
        return buffer.g8()
    }

    public inline fun p8(value: Long): JagByteBuf {
        buffer.p8(value)
        return this
    }

    public inline fun g4f(): Float {
        return buffer.g4f()
    }

    public inline fun p4f(value: Float): JagByteBuf {
        buffer.p4f(value)
        return this
    }

    public inline fun g8d(): Double {
        return buffer.g8d()
    }

    public inline fun p8d(value: Double): JagByteBuf {
        buffer.p8d(value)
        return this
    }

    public inline fun gboolean(): Boolean {
        return buffer.gboolean()
    }

    public inline fun pboolean(value: Boolean): JagByteBuf {
        buffer.pboolean(value)
        return this
    }

    public inline fun fastgjstring(): String? {
        return buffer.fastgjstring()
    }

    public inline fun gjstr(): String {
        return buffer.gjstr()
    }

    public inline fun pjstr(
        s: CharSequence,
        charset: Charset = Cp1252Charset,
    ): JagByteBuf {
        buffer.pjstr(s, charset)
        return this
    }

    public inline fun gjstr2(): String {
        return buffer.gjstr2()
    }

    public inline fun pjstr2(s: CharSequence): JagByteBuf {
        buffer.pjstr2(s)
        return this
    }

    public inline fun gSmart1or2s(): Int {
        return buffer.gSmart1or2s()
    }

    public inline fun pSmart1or2s(value: Int): JagByteBuf {
        buffer.pSmart1or2s(value)
        return this
    }

    public inline fun gSmart1or2(): Int {
        return buffer.gSmart1or2()
    }

    public inline fun pSmart1or2(value: Int): JagByteBuf {
        buffer.pSmart1or2(value)
        return this
    }

    public inline fun gSmart1or2null(): Int {
        return buffer.gSmart1or2null()
    }

    public inline fun pSmart1or2null(value: Int): JagByteBuf {
        buffer.pSmart1or2null(value)
        return this
    }

    public inline fun gSmart1or2extended(): Int {
        return buffer.gSmart1or2extended()
    }

    public inline fun pSmart1or2extended(value: Int): JagByteBuf {
        buffer.pSmart1or2extended(value)
        return this
    }

    public inline fun gSmart2or4(): Int {
        return buffer.gSmart2or4()
    }

    public inline fun pSmart2or4(value: Int): JagByteBuf {
        buffer.pSmart2or4(value)
        return this
    }

    public inline fun gSmart2or4null(): Int {
        return buffer.gSmart2or4null()
    }

    public inline fun pSmart2or4null(value: Int): JagByteBuf {
        buffer.pSmart2or4null(value)
        return this
    }

    public inline fun gVarInt(): Int {
        return buffer.gVarInt()
    }

    public inline fun pVarInt(value: Int): JagByteBuf {
        buffer.pVarInt(value)
        return this
    }

    public inline fun gdata(
        dest: ByteArray,
        offset: Int = 0,
        length: Int = dest.size,
    ) {
        buffer.gdata(dest, offset, length)
    }

    public inline fun gdata(
        dest: ByteBuf,
        offset: Int = buffer.readerIndex(),
        length: Int = buffer.readableBytes(),
    ) {
        buffer.gdata(dest, offset, length)
    }

    public inline fun pdata(
        src: ByteArray,
        start: Int = 0,
        end: Int = src.size,
    ): JagByteBuf {
        buffer.pdata(src, start, end)
        return this
    }

    public inline fun pdata(
        src: ByteBuf,
        start: Int = src.readerIndex(),
        end: Int = (start + src.readableBytes()),
    ): JagByteBuf {
        buffer.pdata(src, start, end)
        return this
    }

    public inline fun gdataAlt1(
        dest: ByteArray,
        offset: Int = 0,
        length: Int = dest.size,
    ) {
        buffer.gdataAlt1(dest, offset, length)
    }

    public inline fun gdataAlt1(
        dest: ByteBuf,
        offset: Int = buffer.readerIndex(),
        length: Int = buffer.readableBytes(),
    ) {
        buffer.gdataAlt1(dest, offset, length)
    }

    public inline fun pdataAlt1(
        src: ByteArray,
        start: Int = 0,
        end: Int = src.size,
    ): JagByteBuf {
        buffer.pdataAlt1(src, start, end)
        return this
    }

    public inline fun pdataAlt1(
        src: ByteBuf,
        start: Int = src.readerIndex(),
        end: Int = (start + src.readableBytes()),
    ): JagByteBuf {
        buffer.pdataAlt1(src, start, end)
        return this
    }

    public inline fun gdataAlt2(
        dest: ByteArray,
        offset: Int = 0,
        length: Int = dest.size,
    ) {
        buffer.gdataAlt2(dest, offset, length)
    }

    public inline fun gdataAlt2(
        dest: ByteBuf,
        offset: Int = buffer.readerIndex(),
        length: Int = buffer.readableBytes(),
    ) {
        buffer.gdataAlt2(dest, offset, length)
    }

    public inline fun pdataAlt2(
        src: ByteArray,
        start: Int = 0,
        end: Int = src.size,
    ): JagByteBuf {
        buffer.pdataAlt2(src, start, end)
        return this
    }

    public inline fun pdataAlt2(
        src: ByteBuf,
        start: Int = src.readerIndex(),
        end: Int = (start + src.readableBytes()),
    ): JagByteBuf {
        buffer.pdataAlt2(src, start, end)
        return this
    }
}
