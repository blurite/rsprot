package net.rsprot.buffer.extensions

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import kotlin.test.Test
import kotlin.test.assertEquals

class ByteBufByteTest {
    private fun wrappedBuffer(vararg bytes: Byte): ByteBuf {
        return Unpooled.wrappedBuffer(bytes)
    }

    /**
     * Using integers to avoid .toByte() casts in tests as byte modifications are often involved.
     */
    private fun wrappedBuffer(vararg bytes: Int): ByteBuf {
        return wrappedBuffer(*bytes.map(Int::toByte).toByteArray())
    }

    @Test
    fun `test g1`() {
        val buffer = wrappedBuffer(0, 150, 255)
        assertEquals(0, buffer.g1())
        assertEquals(150, buffer.g1())
        assertEquals(255, buffer.g1())
    }

    @Test
    fun `test g1s`() {
        val buffer = wrappedBuffer(0, -106, -1)
        assertEquals(0, buffer.g1s())
        assertEquals(-106, buffer.g1s())
        assertEquals(-1, buffer.g1s())
    }

    @Test
    fun `test g1 alt1`() {
        val buffer = wrappedBuffer(0 + HALF_BYTE, 150 + HALF_BYTE, 255 + HALF_BYTE)
        assertEquals(0, buffer.g1Alt1())
        assertEquals(150, buffer.g1Alt1())
        assertEquals(255, buffer.g1Alt1())
    }

    @Test
    fun `test g1s alt1`() {
        val buffer = wrappedBuffer(0 + HALF_BYTE, -106 + HALF_BYTE, -1 + HALF_BYTE)
        assertEquals(0, buffer.g1sAlt1())
        assertEquals(-106, buffer.g1sAlt1())
        assertEquals(-1, buffer.g1sAlt1())
    }

    @Test
    fun `test g1 alt2`() {
        val buffer = wrappedBuffer(-(0), -(150), -(255))
        assertEquals(0, buffer.g1Alt2())
        assertEquals(150, buffer.g1Alt2())
        assertEquals(255, buffer.g1Alt2())
    }

    @Test
    fun `test g1s alt2`() {
        val buffer = wrappedBuffer(-(0), -(-106), -(-1))
        assertEquals(0, buffer.g1sAlt2())
        assertEquals(-106, buffer.g1sAlt2())
        assertEquals(-1, buffer.g1sAlt2())
    }

    @Test
    fun `test g1 alt3`() {
        val buffer = wrappedBuffer(HALF_BYTE - 0, HALF_BYTE - 150, HALF_BYTE - 255)
        assertEquals(0, buffer.g1Alt3())
        assertEquals(150, buffer.g1Alt3())
        assertEquals(255, buffer.g1Alt3())
    }

    @Test
    fun `test g1s alt3`() {
        val buffer = wrappedBuffer(HALF_BYTE - 0, HALF_BYTE - (-106), HALF_BYTE - (-1))
        assertEquals(0, buffer.g1sAlt3())
        assertEquals(-106, buffer.g1sAlt3())
        assertEquals(-1, buffer.g1sAlt3())
    }

    @Test
    fun `test p1`() {
        val buffer = buffer()
        buffer.p1(0)
        buffer.p1(150)
        buffer.p1(255)
        assertEquals(0, buffer.g1())
        assertEquals(150, buffer.g1())
        assertEquals(255, buffer.g1())
    }

    @Test
    fun `test p1 alt1`() {
        val buffer = buffer()
        buffer.p1Alt1(0)
        buffer.p1Alt1(150)
        buffer.p1Alt1(255)
        assertEquals(0, buffer.g1Alt1())
        assertEquals(150, buffer.g1Alt1())
        assertEquals(255, buffer.g1Alt1())
    }

    @Test
    fun `test p1 alt2`() {
        val buffer = buffer()
        buffer.p1Alt2(0)
        buffer.p1Alt2(150)
        buffer.p1Alt2(255)
        assertEquals(0, buffer.g1Alt2())
        assertEquals(150, buffer.g1Alt2())
        assertEquals(255, buffer.g1Alt2())
    }

    @Test
    fun `test p1 alt3`() {
        val buffer = buffer()
        buffer.p1Alt3(0)
        buffer.p1Alt3(150)
        buffer.p1Alt3(255)
        assertEquals(0, buffer.g1Alt3())
        assertEquals(150, buffer.g1Alt3())
        assertEquals(255, buffer.g1Alt3())
    }

    private companion object {
        private const val HALF_BYTE = 0x80
    }
}
