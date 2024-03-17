package net.rsprot.buffer.extensions

import io.netty.buffer.ByteBuf
import kotlin.test.Test
import kotlin.test.assertEquals

class ByteBufIntTest {
    // Only do signed tests as buffer functions always return signed Int type
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
        writer(buffer, -0x3b9ac9db)
        writer(buffer, 0xFFFFFFFF.toInt())

        assertEquals(0, reader(buffer))
        assertEquals(0x80, reader(buffer))
        assertEquals(0xBCA3, reader(buffer))
        assertEquals(0xFFFF, reader(buffer))
        assertEquals(0xBCA3D1, reader(buffer))
        assertEquals(0xFFFFFF, reader(buffer))
        assertEquals(-0x3b9ac9db, reader(buffer))
        assertEquals(-1, reader(buffer))
    }

    @Test
    fun `test g4`() {
        signedBufferReadWriteTest(
            writer = { p4(it) },
            reader = { g4() },
        )
    }

    @Test
    fun `test g4 alt1`() {
        signedBufferReadWriteTest(
            writer = { p4Alt1(it) },
            reader = { g4Alt1() },
        )
    }

    @Test
    fun `test g4 alt2`() {
        signedBufferReadWriteTest(
            writer = { p4Alt2(it) },
            reader = { g4Alt2() },
        )
    }

    @Test
    fun `test g4 alt3`() {
        signedBufferReadWriteTest(
            writer = { p4Alt3(it) },
            reader = { g4Alt3() },
        )
    }
}
