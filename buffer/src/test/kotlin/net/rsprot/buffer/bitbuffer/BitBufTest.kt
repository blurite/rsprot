package net.rsprot.buffer.bitbuffer

import io.netty.buffer.Unpooled
import net.rsprot.buffer.extensions.g1
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals

class BitBufTest {
    private fun buffer(): BitBuf {
        return BitBuf(Unpooled.buffer(100))
    }

    @Test
    fun `writing and reading`() {
        buffer().use { buffer ->
            buffer.pBits(1, 0)
            buffer.pBits(1, 1)
            buffer.pBits(2, 1)
            buffer.pBits(3, 5)
            buffer.pBits(4, 11)
            buffer.pBits(5, 28)
            buffer.pBits(6, 50)
            buffer.pBits(7, 101)
            buffer.pBits(8, 240)
            buffer.pBits(15, 25252)
            buffer.pBits(20, 1048575)
            buffer.pBits(25, 33554431)
            buffer.pBits(31, Int.MAX_VALUE)
            buffer.pBits(32, -1)
            assertEquals(0, buffer.gBits(1))
            assertEquals(1, buffer.gBits(1))
            assertEquals(1, buffer.gBits(2))
            assertEquals(5, buffer.gBits(3))
            assertEquals(11, buffer.gBits(4))
            assertEquals(28, buffer.gBits(5))
            assertEquals(50, buffer.gBits(6))
            assertEquals(101, buffer.gBits(7))
            assertEquals(240, buffer.gBits(8))
            assertEquals(25252, buffer.gBits(15))
            assertEquals(1048575, buffer.gBits(20))
            assertEquals(33554431, buffer.gBits(25))
            assertEquals(Int.MAX_VALUE, buffer.gBits(31))
            assertEquals(-1, buffer.gBits(32))
        }
    }

    @Test
    fun `buffer wrapping on close`() {
        val buffer = Unpooled.buffer()
        BitBuf(buffer).use { bitbuf ->
            bitbuf.pBits(1, 1)
        }
        assertEquals(1, buffer.readableBytes())
        assertEquals(0x80, buffer.g1())
    }

    @Test
    fun `out of bounds writing`() {
        buffer().use { buffer ->
            assertThrows<IllegalArgumentException> {
                buffer.pBits(0, 0)
            }
            assertThrows<IllegalArgumentException> {
                buffer.pBits(33, 0)
            }
        }
    }

    @Test
    fun `out of bounds reading`() {
        buffer().use { buffer ->
            assertThrows<IllegalArgumentException> {
                buffer.gBits(0)
            }
            assertThrows<IllegalArgumentException> {
                buffer.gBits(33)
            }
        }
    }

    @Test
    fun `unreadable buffer reading`() {
        BitBuf(Unpooled.buffer(0, 0)).use { buffer ->
            assertThrows<IllegalArgumentException> {
                buffer.gBits(1)
            }
        }
    }

    @Test
    fun `non writable buffer writing`() {
        BitBuf(Unpooled.buffer(0, 0)).use { buffer ->
            assertThrows<IndexOutOfBoundsException> {
                buffer.pBits(1, 0)
            }
        }
    }
}
