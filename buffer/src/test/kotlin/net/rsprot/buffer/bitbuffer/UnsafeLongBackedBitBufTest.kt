package net.rsprot.buffer.bitbuffer

import io.netty.buffer.Unpooled
import org.junit.jupiter.api.Assertions.assertTrue
import kotlin.test.Test
import kotlin.test.assertEquals

class UnsafeLongBackedBitBufTest {
    private fun buffer(): UnsafeLongBackedBitBuf {
        return UnsafeLongBackedBitBuf()
    }

    @Test
    fun `simple writing and reading`() {
        val buffer = buffer()
        buffer.pBits(1, 1)
        buffer.pBits(2, 2)
        assertEquals(1, buffer.gBits(1))
        assertEquals(2, buffer.gBits(2))
    }

    @Test
    fun `writing and reading`() {
        val buffer = buffer()
        buffer.pBits(1, 1)
        buffer.pBits(2, 2)
        buffer.pBits(3, 1)
        buffer.pBits(4, 1)
        buffer.pBits(5, 8)
        buffer.pBits(6, 1)
        buffer.pBits(7, 71)
        buffer.pBits(8, 1)
        buffer.pBits(9, 80)
        assertEquals(1, buffer.gBits(1))
        assertEquals(2, buffer.gBits(2))
        assertEquals(1, buffer.gBits(3))
        assertEquals(1, buffer.gBits(4))
        assertEquals(8, buffer.gBits(5))
        assertEquals(1, buffer.gBits(6))
        assertEquals(71, buffer.gBits(7))
        assertEquals(1, buffer.gBits(8))
        assertEquals(80, buffer.gBits(9))
    }

    @Test
    fun `writing, copying and reading`() {
        val buffer = buffer()
        buffer.pBits(1, 1)
        buffer.pBits(2, 2)
        buffer.pBits(3, 1)
        buffer.pBits(4, 1)
        buffer.pBits(5, 8)
        buffer.pBits(6, 1)
        buffer.pBits(7, 71)
        buffer.pBits(8, 1)
        buffer.pBits(9, 80)
        val copy = BitBuf(Unpooled.buffer())
        copy.pBits(buffer)
        assertEquals(1, copy.gBits(1))
        assertEquals(2, copy.gBits(2))
        assertEquals(1, copy.gBits(3))
        assertEquals(1, copy.gBits(4))
        assertEquals(8, copy.gBits(5))
        assertEquals(1, copy.gBits(6))
        assertEquals(71, copy.gBits(7))
        assertEquals(1, copy.gBits(8))
        assertEquals(80, copy.gBits(9))
        assertTrue(!copy.isReadable())
    }
}
