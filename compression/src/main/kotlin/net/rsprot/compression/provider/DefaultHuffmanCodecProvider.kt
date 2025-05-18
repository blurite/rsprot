package net.rsprot.compression.provider

import net.rsprot.compression.HuffmanCodec

/**
 * A default Huffman codec provider that takes a preloaded codec in and returns it
 * without any sort of extra steps.
 * @property huffmanCodec the huffman codec to supply.
 */
public class DefaultHuffmanCodecProvider(
    private val huffmanCodec: HuffmanCodec,
) : HuffmanCodecProvider {
    override fun provide(): HuffmanCodec {
        return huffmanCodec
    }
}
