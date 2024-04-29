package net.rsprot.compression.provider

import net.rsprot.compression.HuffmanCodec

/**
 * A blocking Huffman codec provider that blocks the calling thread if a Huffman
 * codec has not yet been supplied. All calling threads will be woken up when
 * the server supplies a huffman codec.
 * @property lock the object to wait on if a Huffman codec has not been provided yet.
 * @property huffmanCodec the Huffman codec to provide when it has been supplied.
 */
public class BlockingHuffmanCodecProvider : HuffmanCodecProvider {
    @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
    private val lock: Object = Object()

    @Volatile
    private var huffmanCodec: HuffmanCodec? = null

    override fun provide(): HuffmanCodec {
        val codec = huffmanCodec
        if (codec == null) {
            synchronized(lock) {
                lock.wait()
            }
            return checkNotNull(huffmanCodec)
        }
        return codec
    }

    /**
     * Supplies a Huffman codec instance to this provider, which in return also
     * wakes up any threads waiting on the lock.
     * @param codec the Huffman codec to supply.
     */
    public fun supply(codec: HuffmanCodec) {
        require(this.huffmanCodec == null) {
            "Huffman codec already initialized!"
        }
        this.huffmanCodec = codec
        synchronized(lock) {
            lock.notifyAll()
        }
    }
}
