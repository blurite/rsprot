package net.rsprot.buffer.extensions

import io.netty.buffer.ByteBuf
import kotlin.test.Test
import kotlin.test.assertEquals

class ByteBufMediumTest {
    private fun unsignedBufferReadWriteTest(
        writer: ByteBuf.(value: Int) -> Unit,
        reader: ByteBuf.() -> Int,
    ) {
        val buffer = buffer()
        writer(buffer, 0)
        writer(buffer, 0x80)
        writer(buffer, 0xBCA3)
        writer(buffer, 0xFFFF)
        writer(buffer, 0xBCA3D1)
        writer(buffer, 0xFFFFFF)

        assertEquals(0, reader(buffer))
        assertEquals(0x80, reader(buffer))
        assertEquals(0xBCA3, reader(buffer))
        assertEquals(0xFFFF, reader(buffer))
        assertEquals(0xBCA3D1, reader(buffer))
        assertEquals(0xFFFFFF, reader(buffer))
    }

    private fun signedBufferReadWriteTest(
        writer: ByteBuf.(value: Int) -> Unit,
        reader: ByteBuf.() -> Int,
    ) {
        val buffer = buffer()
        writer(buffer, 0)
        writer(buffer, 0x80)
        writer(buffer, 0xBCA3)
        writer(buffer, 0xFFFF)
        writer(buffer, 0xBCA3D1)
        writer(buffer, 0xFFFFFF)

        assertEquals(0, reader(buffer))
        assertEquals(0x80, reader(buffer))
        assertEquals(0xBCA3, reader(buffer))
        assertEquals(0xFFFF, reader(buffer))
        assertEquals(-4414511, reader(buffer))
        assertEquals(-1, reader(buffer))
    }

    @Test
    fun `test g3`() {
        unsignedBufferReadWriteTest(
            writer = { p3(it) },
            reader = { g3() },
        )
    }

    @Test
    fun `test g3s`() {
        signedBufferReadWriteTest(
            writer = { p3(it) },
            reader = { g3s() },
        )
    }

    @Test
    fun `test g3 alt1`() {
        unsignedBufferReadWriteTest(
            writer = { p3Alt1(it) },
            reader = { g3Alt1() },
        )
    }

    @Test
    fun `test g3s alt1`() {
        signedBufferReadWriteTest(
            writer = { p3Alt1(it) },
            reader = { g3sAlt1() },
        )
    }

    @Test
    fun `test g3 alt2`() {
        unsignedBufferReadWriteTest(
            writer = { p3Alt2(it) },
            reader = { g3Alt2() },
        )
    }

    @Test
    fun `test g3s alt2`() {
        signedBufferReadWriteTest(
            writer = { p3Alt2(it) },
            reader = { g3sAlt2() },
        )
    }

    @Test
    fun `test g3 alt3`() {
        unsignedBufferReadWriteTest(
            writer = { p3Alt3(it) },
            reader = { g3Alt3() },
        )
    }

    @Test
    fun `test g3s alt3`() {
        signedBufferReadWriteTest(
            writer = { p3Alt3(it) },
            reader = { g3sAlt3() },
        )
    }
}
