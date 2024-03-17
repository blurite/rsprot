package net.rsprot.buffer.extensions

import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ByteBufSmartTest {
    @Test
    fun `test gSmart1or2`() {
        val buffer = buffer()
        buffer.pSmart1or2(0)
        buffer.pSmart1or2(0x0F)
        buffer.pSmart1or2(0x7F)
        buffer.pSmart1or2(0xFF)
        buffer.pSmart1or2(0xABC)
        buffer.pSmart1or2(0x7FFF)
        assertThrows<IllegalArgumentException> {
            buffer.pSmart1or2(-1)
        }
        assertThrows<IllegalArgumentException> {
            buffer.pSmart1or2(0x8000)
        }
        assertEquals(0, buffer.gSmart1or2())
        assertEquals(0x0F, buffer.gSmart1or2())
        assertEquals(0x7F, buffer.gSmart1or2())
        assertEquals(0xFF, buffer.gSmart1or2())
        assertEquals(0xABC, buffer.gSmart1or2())
        assertEquals(0x7FFF, buffer.gSmart1or2())
        assertTrue(!buffer.isReadable)
    }

    @Test
    fun `test gSmart1or2s`() {
        val buffer = buffer()
        buffer.pSmart1or2s(0)
        buffer.pSmart1or2s(-0x64)
        buffer.pSmart1or2s(0x3F)
        buffer.pSmart1or2s(0x7F)
        buffer.pSmart1or2s(-0x7F)
        buffer.pSmart1or2s(0xABC)
        buffer.pSmart1or2s(-0xABC)
        buffer.pSmart1or2s(-0x4000)
        buffer.pSmart1or2s(0x3FFF)
        assertThrows<IllegalArgumentException> {
            buffer.pSmart1or2s(-0x4001)
        }
        assertThrows<IllegalArgumentException> {
            buffer.pSmart1or2s(0x4000)
        }
        assertEquals(0, buffer.gSmart1or2s())
        assertEquals(-0x64, buffer.gSmart1or2s())
        assertEquals(0x3F, buffer.gSmart1or2s())
        assertEquals(0x7F, buffer.gSmart1or2s())
        assertEquals(-0x7F, buffer.gSmart1or2s())
        assertEquals(0xABC, buffer.gSmart1or2s())
        assertEquals(-0xABC, buffer.gSmart1or2s())
        assertEquals(-0x4000, buffer.gSmart1or2s())
        assertEquals(0x3FFF, buffer.gSmart1or2s())
        assertTrue(!buffer.isReadable)
    }

    @Test
    fun `test gSmart1or2null`() {
        val buffer = buffer()
        buffer.pSmart1or2null(0)
        buffer.pSmart1or2null(0x0F)
        buffer.pSmart1or2null(0x7F)
        buffer.pSmart1or2null(0xFF)
        buffer.pSmart1or2null(0xABC)
        buffer.pSmart1or2null(0x7FFE)
        assertThrows<IllegalArgumentException> {
            buffer.pSmart1or2null(-2)
        }
        assertThrows<IllegalArgumentException> {
            buffer.pSmart1or2null(0x7FFF)
        }
        assertEquals(0, buffer.gSmart1or2null())
        assertEquals(0x0F, buffer.gSmart1or2null())
        assertEquals(0x7F, buffer.gSmart1or2null())
        assertEquals(0xFF, buffer.gSmart1or2null())
        assertEquals(0xABC, buffer.gSmart1or2null())
        assertEquals(0x7FFE, buffer.gSmart1or2null())
        assertTrue(!buffer.isReadable)
    }

    @Test
    fun `test gSmart1or2extended`() {
        val buffer = buffer()
        buffer.pSmart1or2extended(0)
        buffer.pSmart1or2extended(0x0F)
        buffer.pSmart1or2extended(0x7F)
        buffer.pSmart1or2extended(0xFF)
        buffer.pSmart1or2extended(0xABC)
        buffer.pSmart1or2extended(0x7FFF)
        buffer.pSmart1or2extended(0x7FFFF)
        assertThrows<IllegalArgumentException> {
            buffer.pSmart1or2extended(-1)
        }
        assertEquals(0, buffer.gSmart1or2extended())
        assertEquals(0x0F, buffer.gSmart1or2extended())
        assertEquals(0x7F, buffer.gSmart1or2extended())
        assertEquals(0xFF, buffer.gSmart1or2extended())
        assertEquals(0xABC, buffer.gSmart1or2extended())
        assertEquals(0x7FFF, buffer.gSmart1or2extended())
        assertEquals(0x7FFFF, buffer.gSmart1or2extended())
        assertTrue(!buffer.isReadable)
    }

    @Test
    fun `test gSmart2or4`() {
        val buffer = buffer()
        buffer.pSmart2or4(0)
        buffer.pSmart2or4(0x0F)
        buffer.pSmart2or4(0x7F)
        buffer.pSmart2or4(0xFF)
        buffer.pSmart2or4(0xABC)
        buffer.pSmart2or4(0x7FFF)
        buffer.pSmart2or4(0xFFFF)
        buffer.pSmart2or4(0xFFFFFF)
        buffer.pSmart2or4(0x7FFFFFFF)
        assertThrows<IllegalArgumentException> {
            buffer.pSmart2or4(-1)
        }
        assertEquals(0, buffer.gSmart2or4())
        assertEquals(0x0F, buffer.gSmart2or4())
        assertEquals(0x7F, buffer.gSmart2or4())
        assertEquals(0xFF, buffer.gSmart2or4())
        assertEquals(0xABC, buffer.gSmart2or4())
        assertEquals(0x7FFF, buffer.gSmart2or4())
        assertEquals(0xFFFF, buffer.gSmart2or4())
        assertEquals(0xFFFFFF, buffer.gSmart2or4())
        assertEquals(0x7FFFFFFF, buffer.gSmart2or4())
        assertTrue(!buffer.isReadable)
    }

    @Test
    fun `test gSmart2or4null`() {
        val buffer = buffer()
        buffer.pSmart2or4null(0)
        buffer.pSmart2or4null(0x0F)
        buffer.pSmart2or4null(0x7F)
        buffer.pSmart2or4null(0xFF)
        buffer.pSmart2or4null(0xABC)
        buffer.pSmart2or4null(0x7FFF)
        buffer.pSmart2or4null(0xFFFF)
        buffer.pSmart2or4null(0xFFFFFF)
        buffer.pSmart2or4null(0x7FFFFFFE)
        buffer.pSmart2or4null(-1)
        assertThrows<IllegalArgumentException> {
            buffer.pSmart2or4null(-2)
        }
        assertThrows<IllegalArgumentException> {
            buffer.pSmart2or4null(0x7FFFFFFF)
        }
        assertEquals(0, buffer.gSmart2or4null())
        assertEquals(0x0F, buffer.gSmart2or4null())
        assertEquals(0x7F, buffer.gSmart2or4null())
        assertEquals(0xFF, buffer.gSmart2or4null())
        assertEquals(0xABC, buffer.gSmart2or4null())
        assertEquals(0x7FFF, buffer.gSmart2or4null())
        assertEquals(0xFFFF, buffer.gSmart2or4null())
        assertEquals(0xFFFFFF, buffer.gSmart2or4null())
        assertEquals(0x7FFFFFFE, buffer.gSmart2or4null())
        assertEquals(-1, buffer.gSmart2or4null())
        assertTrue(!buffer.isReadable)
    }
}
