package net.rsprot.buffer.extensions

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import kotlin.test.Test
import kotlin.test.assertContentEquals

class ByteBufArrayTest {
    private fun wrappedBuffer(
        @Suppress("SameParameterValue") array: ByteArray,
    ): ByteBuf {
        return Unpooled.wrappedBuffer(array)
    }

    private fun asByteArray(buffer: ByteBuf): ByteArray {
        return ByteArray(buffer.readableBytes(), buffer::getByte)
    }

    @Test
    fun `test gdata`() {
        val buffer = wrappedBuffer(testData)
        val output = ByteArray(testData.size)
        buffer.gdata(output)
        assertContentEquals(testData, output)
    }

    @Test
    fun `test pdata`() {
        val buffer = buffer()
        buffer.pdata(testData)
        assertContentEquals(testData, asByteArray(buffer))
    }

    @Test
    fun `test gdata alt1`() {
        val buffer = wrappedBuffer(testData.reversedArray())
        val output = ByteArray(testData.size)
        buffer.gdataAlt1(output)
        assertContentEquals(testData, output)
    }

    @Test
    fun `test pdata alt1`() {
        val buffer = buffer()
        buffer.pdataAlt1(testData)
        assertContentEquals(testData.reversedArray(), asByteArray(buffer))
    }

    @Test
    fun `test gdata alt2`() {
        val data =
            testData
                .reversedArray()
                .map { (it - 0x80).toByte() }
                .toByteArray()
        val buffer = wrappedBuffer(data)
        val output = ByteArray(testData.size)
        buffer.gdataAlt2(output)
        assertContentEquals(testData, output)
    }

    @Test
    fun `test pdata alt2`() {
        val buffer = buffer()
        buffer.pdataAlt2(testData)
        val expected =
            testData
                .reversedArray()
                .map { (it - 0x80).toByte() }
                .toByteArray()
        assertContentEquals(expected, asByteArray(buffer))
    }

    private companion object {
        private val testData = byteArrayOf(0, -0x80, 0x7F, -0x64, 0x64, 0x1, -0x1)
    }
}
