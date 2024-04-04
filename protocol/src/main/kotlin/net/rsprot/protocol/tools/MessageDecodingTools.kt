package net.rsprot.protocol.tools

import net.rsprot.compression.HuffmanCodec

/**
 * Message decoding tools are a set of tools passed to message decoders,
 * as some packets require a special set of tools to be decoded (e.g.
 * huffman codec for message packets, used to decompress the messages).
 * Rather than pass in these tools directly in the decoders, we utilize
 * a wrapper class to make future changes less abrasive - if we need
 * to introduce new tools, we just have to insert them into this class,
 * rather than needing to modify the arguments of every incoming packet
 * ever created.
 *
 * @property huffmanCodec huffman codec used to compress and decompress
 * chat messages.
 */
public class MessageDecodingTools(
    public val huffmanCodec: HuffmanCodec,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MessageDecodingTools

        return huffmanCodec == other.huffmanCodec
    }

    override fun hashCode(): Int {
        return huffmanCodec.hashCode()
    }

    override fun toString(): String {
        return "MessageDecodingTools(huffmanCodec=$huffmanCodec)"
    }
}
