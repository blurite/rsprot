package net.rsprot.compression.provider

import net.rsprot.compression.HuffmanCodec

/**
 * An interface that provides a huffman codec whenever it is ready.
 */
public sealed interface HuffmanCodecProvider {
    /**
     * Provides the huffman codec instance.
     * If a blocking implementation is used, this will block up the calling threads
     * until it has been supplied with a codec.
     */
    public fun provide(): HuffmanCodec
}
