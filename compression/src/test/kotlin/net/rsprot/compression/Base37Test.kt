package net.rsprot.compression

import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals

class Base37Test {
    @Test
    fun `empty string encoding`() {
        val encoded = Base37.encode("")
        assertEquals(0L, encoded)
    }

    @Test
    fun `length out of bounds encoding`() {
        val value = Base37.encode("1234567890abc")
        assertEquals(5125153220596123482, value)
    }

    @Test
    fun `short string encoding`() {
        val encoded = Base37.encode("test")
        assertEquals(1020628, encoded)
    }

    @Test
    fun `long string encoding`() {
        val encoded = Base37.encode("999999999999")
        assertEquals(6582952005840035280L, encoded)
    }

    @Test
    fun `string with spaces encoding`() {
        val encoded = Base37.encode("hello world")
        assertEquals(39161811144077548L, encoded)
    }

    @Test
    fun `empty string decoding`() {
        val decoded = Base37.decode(0L)
        assertEquals("", decoded)
    }

    @Test
    fun `invalid value decoding`() {
        assertThrows<IllegalArgumentException>("Input value is a valid base-37 string") {
            Base37.decode(Long.MAX_VALUE)
        }
    }

    @Test
    fun `short string decoding`() {
        val decoded = Base37.decode(1020628)
        assertEquals("test", decoded)
    }

    @Test
    fun `long string decoding`() {
        val decoded = Base37.decode(6582952005840035280L)
        assertEquals("999999999999", decoded)
    }

    @Test
    fun `string with underscores decoding`() {
        val decoded = Base37.decode(39161811144077548L)
        assertEquals("hello_world", decoded)
    }

    @Test
    fun `string with nbsp uppercase decoding`() {
        val decoded = Base37.decodeWithCase(39161811144077548L)
        assertEquals("Hello World", decoded)
    }
}
