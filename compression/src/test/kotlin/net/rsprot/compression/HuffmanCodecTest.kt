package net.rsprot.compression

import io.netty.buffer.Unpooled
import net.rsprot.buffer.extensions.toByteArray
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import kotlin.test.assertContentEquals

class HuffmanCodecTest {
    @ParameterizedTest
    @MethodSource("readSampleText")
    fun `lorem ipsum encodes correctly`(sample: Pair<String, ByteArray>) {
        val huffman = createHuffmanCodec()
        val buffer = Unpooled.buffer(15_000)
        huffman.encode(buffer, sample.first)
        val array = buffer.toByteArray()
        assertContentEquals(sample.second, array)
    }

    @ParameterizedTest
    @MethodSource("readSampleText")
    fun `lorem ipsum decodes correctly`(sample: Pair<String, ByteArray>) {
        val huffman = createHuffmanCodec()
        val decoded = huffman.decode(Unpooled.wrappedBuffer(sample.second))
        assertEquals(sample.first, decoded)
    }

    companion object {
        @JvmStatic
        private fun readSampleText(): Stream<Pair<String, ByteArray>> {
            val compressed = readCompressedLoremIpsum()
            val text = readTextLoremIpsum()
            return Stream.of(text to compressed)
        }

        private fun createHuffmanCodec(): HuffmanCodec {
            val resource = HuffmanCodec::class.java.getResourceAsStream("huffman.dat")
            checkNotNull(resource) {
                "huffman.dat could not be found"
            }
            return HuffmanCodec.create(Unpooled.wrappedBuffer(resource.readBytes()))
        }

        private fun readCompressedLoremIpsum(): ByteArray {
            val resource = HuffmanCodec::class.java.getResourceAsStream("lorem_ipsum_compressed.dat")
            checkNotNull(resource) {
                "lorem_ipsum_compressed.dat could not be found could not be found"
            }
            return resource.readBytes()
        }

        private fun readTextLoremIpsum(): String {
            val resource = HuffmanCodec::class.java.getResourceAsStream("lorem_ipsum_sample.txt")
            checkNotNull(resource) {
                "lorem_ipsum_sample.txt could not be found could not be found"
            }
            return String(resource.readBytes())
        }
    }
}
