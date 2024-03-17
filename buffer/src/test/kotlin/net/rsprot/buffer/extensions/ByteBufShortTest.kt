package net.rsprot.buffer.extensions

import kotlin.test.Test
import kotlin.test.assertEquals

class ByteBufShortTest {
    @Test
    fun `test g2`() {
        val buffer = buffer()
        buffer.p2(0)
        buffer.p2(0x80)
        buffer.p2(0xBCA3)
        buffer.p2(0xFFFF)
        assertEquals(0, buffer.g2())
        assertEquals(0x80, buffer.g2())
        assertEquals(0xBCA3, buffer.g2())
        assertEquals(0xFFFF, buffer.g2())
    }

    @Test
    fun `test g2s`() {
        val buffer = buffer()
        buffer.p2(0)
        buffer.p2(0x80)
        buffer.p2(0xBCA3)
        buffer.p2(0xFFFF)
        assertEquals(0, buffer.g2s())
        assertEquals(0x80, buffer.g2s())
        assertEquals(-17245, buffer.g2s())
        assertEquals(-1, buffer.g2s())
    }

    @Test
    fun `test g2 alt1`() {
        val buffer = buffer()
        buffer.p2Alt1(0)
        buffer.p2Alt1(0x80)
        buffer.p2Alt1(0xBCA3)
        buffer.p2Alt1(0xFFFF)
        assertEquals(0, buffer.g2Alt1())
        assertEquals(0x80, buffer.g2Alt1())
        assertEquals(0xBCA3, buffer.g2Alt1())
        assertEquals(0xFFFF, buffer.g2Alt1())
    }

    @Test
    fun `test g2s alt1`() {
        val buffer = buffer()
        buffer.p2Alt1(0)
        buffer.p2Alt1(0x80)
        buffer.p2Alt1(0xBCA3)
        buffer.p2Alt1(0xFFFF)
        assertEquals(0, buffer.g2sAlt1())
        assertEquals(0x80, buffer.g2sAlt1())
        assertEquals(-17245, buffer.g2sAlt1())
        assertEquals(-1, buffer.g2sAlt1())
    }

    @Test
    fun `test g2 alt2`() {
        val buffer = buffer()
        buffer.p2Alt2(0)
        buffer.p2Alt2(0x80)
        buffer.p2Alt2(0xBCA3)
        buffer.p2Alt2(0xFFFF)
        assertEquals(0, buffer.g2Alt2())
        assertEquals(0x80, buffer.g2Alt2())
        assertEquals(0xBCA3, buffer.g2Alt2())
        assertEquals(0xFFFF, buffer.g2Alt2())
    }

    @Test
    fun `test g2s alt2`() {
        val buffer = buffer()
        buffer.p2Alt2(0)
        buffer.p2Alt2(0x80)
        buffer.p2Alt2(0xBCA3)
        buffer.p2Alt2(0xFFFF)
        assertEquals(0, buffer.g2sAlt2())
        assertEquals(0x80, buffer.g2sAlt2())
        assertEquals(-17245, buffer.g2sAlt2())
        assertEquals(-1, buffer.g2sAlt2())
    }

    @Test
    fun `test g2 alt3`() {
        val buffer = buffer()
        buffer.p2Alt3(0)
        buffer.p2Alt3(0x80)
        buffer.p2Alt3(0xBCA3)
        buffer.p2Alt3(0xFFFF)
        assertEquals(0, buffer.g2Alt3())
        assertEquals(0x80, buffer.g2Alt3())
        assertEquals(0xBCA3, buffer.g2Alt3())
        assertEquals(0xFFFF, buffer.g2Alt3())
    }

    @Test
    fun `test g2s alt3`() {
        val buffer = buffer()
        buffer.p2Alt3(0)
        buffer.p2Alt3(0x80)
        buffer.p2Alt3(0xBCA3)
        buffer.p2Alt3(0xFFFF)
        assertEquals(0, buffer.g2sAlt3())
        assertEquals(0x80, buffer.g2sAlt3())
        assertEquals(-17245, buffer.g2sAlt3())
        assertEquals(-1, buffer.g2sAlt3())
    }
}
