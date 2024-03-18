package net.rsprot.buffer.bitbuffer

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import net.rsprot.buffer.bitbuffer.InlineBitBuf.Companion.INDEX_PADDING
import net.rsprot.buffer.extensions.g1
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals

class InlineBitBufTest {
    private fun buffer(): ByteBuf {
        return Unpooled.buffer(100 + INDEX_PADDING)
    }

    @Test
    fun `writing and reading`() {
        InlineBitBuf.use(buffer()) {
            pBits(1, 0)
            pBits(1, 1)
            pBits(2, 1)
            pBits(3, 5)
            pBits(4, 11)
            pBits(5, 28)
            pBits(6, 50)
            pBits(7, 101)
            pBits(8, 240)
            pBits(15, 25252)
            pBits(20, 1048575)
            pBits(25, 33554431)
            pBits(31, Int.MAX_VALUE)
            pBits(32, -1)
            assertEquals(0, gBits(1))
            assertEquals(1, gBits(1))
            assertEquals(1, gBits(2))
            assertEquals(5, gBits(3))
            assertEquals(11, gBits(4))
            assertEquals(28, gBits(5))
            assertEquals(50, gBits(6))
            assertEquals(101, gBits(7))
            assertEquals(240, gBits(8))
            assertEquals(25252, gBits(15))
            assertEquals(1048575, gBits(20))
            assertEquals(33554431, gBits(25))
            assertEquals(Int.MAX_VALUE, gBits(31))
            assertEquals(-1, gBits(32))
        }
    }

    @Test
    fun `buffer wrapping on close`() {
        val buffer = Unpooled.buffer()
        InlineBitBuf.use(buffer) {
            pBits(1, 1)
        }
        assertEquals(1, buffer.readableBytes())
        assertEquals(0x80, buffer.g1())
    }

    @Test
    fun `out of bounds writing`() {
        InlineBitBuf.use(buffer()) {
            assertThrows<IllegalArgumentException> {
                pBits(0, 0)
            }
            assertThrows<IllegalArgumentException> {
                pBits(33, 0)
            }
        }
    }

    @Test
    fun `out of bounds reading`() {
        InlineBitBuf.use(buffer()) {
            assertThrows<IllegalArgumentException> {
                gBits(0)
            }
            assertThrows<IllegalArgumentException> {
                gBits(33)
            }
        }
    }

    @Test
    fun `unreadable buffer reading`() {
        InlineBitBuf.use(Unpooled.buffer(INDEX_PADDING, INDEX_PADDING)) {
            assertThrows<IllegalArgumentException> {
                gBits(1)
            }
        }
    }

    @Test
    fun `non writable buffer writing`() {
        InlineBitBuf.use(Unpooled.buffer(INDEX_PADDING, INDEX_PADDING)) {
            assertThrows<IndexOutOfBoundsException> {
                pBits(1, 0)
            }
        }
    }
}
