package net.rsprot.buffer.extensions

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ByteBufSpecialTypeTest {
    @Test
    fun `test g8`() {
        val buffer = buffer()
        buffer.p8(0x123456789ABCDEFAL)
        buffer.p8(-0x123456789ABCDEFAL)
        buffer.p8(-1L)
        assertEquals(0x123456789ABCDEFAL, buffer.g8())
        assertEquals(-0x123456789ABCDEFAL, buffer.g8())
        assertEquals(-1L, buffer.g8())
    }

    @Test
    fun `test g4f`() {
        val buffer = buffer()
        buffer.p4f(0.0F)
        buffer.p4f(1.1F)
        buffer.p4f(3.4028235E38F)
        assertEquals(0F, buffer.g4f())
        assertEquals(1.1F, buffer.g4f())
        assertEquals(3.4028235E38F, buffer.g4f())
    }

    @Test
    fun `test g8d`() {
        val buffer = buffer()
        buffer.p8d(0.0)
        buffer.p8d(1.1)
        buffer.p8d(1.7976931348623157E+308)
        assertEquals(0.0, buffer.g8d())
        assertEquals(1.1, buffer.g8d())
        assertEquals(1.7976931348623157E+308, buffer.g8d())
    }

    @Test
    fun `test gboolean`() {
        val buffer = buffer()
        buffer.pboolean(false)
        buffer.pboolean(true)
        assertEquals(false, buffer.gboolean())
        assertEquals(true, buffer.gboolean())
        assertTrue(!buffer.isReadable)
    }

    @Test
    fun `test gjstr`() {
        val buffer = buffer()
        buffer.pjstr("Hello world")
        assertEquals("Hello world", buffer.gjstr())
        assertTrue(!buffer.isReadable)
    }

    @Test
    fun `test fastgjstr`() {
        val buffer = buffer()
        buffer.pjstr("Hello world")
        assertEquals("Hello world", buffer.fastgjstring())
        assertTrue(!buffer.isReadable)
    }

    @Test
    fun `test gjstr2`() {
        val buffer = buffer()
        buffer.pjstr2("Hello world")
        assertEquals("Hello world", buffer.gjstr2())
        assertTrue(!buffer.isReadable)
    }

    @Test
    fun `test gvarint`() {
        val buffer = buffer()
        buffer.pVarInt(0)
        buffer.pVarInt(0xF)
        buffer.pVarInt(0xFF)
        buffer.pVarInt(0xFFFF)
        buffer.pVarInt(0xFFFFFF)
        buffer.pVarInt(0x7FFFFFFF)
        buffer.pVarInt(-1)
        buffer.pVarInt(-0xFFFFFF)
        buffer.pVarInt(-0x7FFFFFFF)
        assertEquals(0, buffer.gVarInt())
        assertEquals(0xF, buffer.gVarInt())
        assertEquals(0xFF, buffer.gVarInt())
        assertEquals(0xFFFF, buffer.gVarInt())
        assertEquals(0xFFFFFF, buffer.gVarInt())
        assertEquals(0x7FFFFFFF, buffer.gVarInt())
        assertEquals(-1, buffer.gVarInt())
        assertEquals(-0xFFFFFF, buffer.gVarInt())
        assertEquals(-0x7FFFFFFF, buffer.gVarInt())
        assertTrue(!buffer.isReadable)
    }
}
