package net.rsprot.compression

import com.github.michaelbull.logging.InlineLogger
import io.netty.buffer.ByteBuf
import net.rsprot.buffer.JagByteBuf
import net.rsprot.buffer.bitbuffer.BitBuf
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprot.buffer.util.charset.Cp1252Charset
import java.io.IOException

/**
 * Huffman codec implementation.
 * This implementation was shared by Graham on Rune-Server.
 * The below implementation has been converted to Kotlin and slightly cleaned up.
 */
@Suppress("MemberVisibilityCanBePrivate")
public data class HuffmanCodec(
    private val bits: IntArray,
    private val codewords: IntArray,
    private val lookupTree: IntArray,
) {
    init {
        require(bits.size == CODEWORDS_LENGTH) {
            "Bits array must be 256 elements long"
        }
        require(codewords.size == CODEWORDS_LENGTH) {
            "Codewords array must be 256 elements long"
        }
    }

    public fun decode(buf: ByteBuf): String {
        return decode(buf.toJagByteBuf())
    }

    public fun decode(buf: JagByteBuf): String {
        val len: Int = buf.gSmart1or2()
        val bytes = ByteArray(len)

        /*
         * See the comments in init() for an introduction to how the lookup
         * tree works. We do the same sort of thing here, but we're reading
         * instead of writing.
         */
        BitBuf(buf.buffer).use { bitBuf ->
            var node = 0 // Start at the root node.
            var i = 0
            while (i < len) {
                if (bitBuf.gBits(1) == 1) {
                    // Go 'right' by setting node := lookupTree[node].
                    node = lookupTree[node]
                } else {
                    // Go 'left' by incrementing the node number.
                    node++
                }

                // Check if the current node is a leaf node.
                val chr = lookupTree[node]
                if (chr and -0x80000000 != 0) {
                    // Un-complement and store the character in the output array.
                    bytes[i++] = chr.inv().toByte()

                    /*
                     * Move the pointer back to the root node of the tree to
                     * start reading the next codeword.
                     */
                    node = 0
                }
            }
        }
        check(!buf.isReadable)
        return String(bytes, Cp1252Charset)
    }

    public fun encode(
        buf: ByteBuf,
        text: String,
    ) {
        return encode(
            buf.toJagByteBuf(),
            text,
        )
    }

    public fun encode(
        buf: JagByteBuf,
        text: String,
    ) {
        val bytes: ByteArray = text.toByteArray(Cp1252Charset)
        require(bytes.size < MAX_LENGTH) {
            "Encoded text length must be strictly less than 32,768 bytes"
        }
        buf.pSmart1or2(bytes.size)
        BitBuf(buf.buffer).use { bitBuf ->
            for (b in bytes) {
                val chr = b.toInt() and 0xFF
                val codeword = codewords[chr]
                val numBits = bits[chr]
                require(numBits != 0) {
                    "No codeword for data value $chr"
                }
                bitBuf.pBits(numBits, codeword)
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as HuffmanCodec

        if (!bits.contentEquals(other.bits)) return false
        if (!codewords.contentEquals(other.codewords)) return false
        if (!lookupTree.contentEquals(other.lookupTree)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = bits.contentHashCode()
        result = 31 * result + codewords.contentHashCode()
        result = 31 * result + lookupTree.contentHashCode()
        return result
    }

    public companion object {
        private val logger = InlineLogger()
        private const val MAX_LENGTH = 32768
        private const val CODEWORDS_LENGTH = 256

        @Throws(IOException::class)
        public fun create(buffer: ByteBuf): HuffmanCodec {
            return create(buffer.toJagByteBuf())
        }

        @Throws(IOException::class)
        public fun create(buffer: JagByteBuf): HuffmanCodec {
            // Read the number of bits used to encode each character from the cache.
            if (buffer.readableBytes() != CODEWORDS_LENGTH) {
                throw IOException(
                    "Huffman file should be exactly $CODEWORDS_LENGTH bytes: ${buffer.readableBytes()}",
                )
            }
            val bits = IntArray(CODEWORDS_LENGTH)
            for (i in bits.indices) {
                bits[i] = buffer.g1()
            }

            // The next codeword to use for each bit length.
            val nextCodewords = IntArray(32)

            // The next free node in the lookupTable.
            var nextFreeNode = 0

            // Allocate the codeword for each character.
            val codewords = IntArray(CODEWORDS_LENGTH)
            var lookupTree = IntArray(8)
            for (chr in bits.indices) {
                val numBits = bits[chr]
                if (numBits == 0) {
                    continue
                }

                // Select the next codeword for this bit length.
                codewords[chr] = nextCodewords[numBits - 1]
                val codeword = codewords[chr]

                /*
                 * Iterate through the next codewords for each bit length from 1 to
                 * the current character's bit length (numBits), in reverse order.
                 */
                for (i in numBits downTo 1) {
                    /*
                     * Check if the next codeword for that length is identical to
                     * the one for the current length.
                     */
                    val nextCodeword = nextCodewords[i - 1]
                    if (codeword != nextCodeword) {
                        break
                    }

                    /*
                     * If so, we need to make it different somehow to avoid the
                     * codeword from being re-used.
                     *
                     * If the 'i'th bit is not set, we set it.
                     *
                     * Otherwise, we change the next codeword to be the same as the
                     * next codeword for the next smallest bit length, or zero if
                     * we're at the last bit. As the iteration is in reverse order,
                     * this copying will cascade until a bit can be set.
                     *
                     * Effectively this all means we just increment the next
                     * codeword, possibly multiple times if we need to skip over a
                     * codeword we can't use.
                     */
                    val bit = 1 shl (32 - i)
                    if (nextCodeword and bit == 0) {
                        nextCodewords[i - 1] = nextCodeword or bit
                    } else if (i != 1) {
                        nextCodewords[i - 1] = nextCodewords[i - 2]
                    } else {
                        nextCodewords[0] = 0
                    }
                }

                /*
                 * Iterate through the next codewords for each bit length from the
                 * character's current bit length (numBits, exclusive) to the
                 * maximum bit length (32). If the next codeword is the same as the
                 * codeword selected for the current character, we update the next
                 * codeword to the next codeword for our current bit length. Again,
                 * this is to ensure a codeword is not re-used.
                 */
                for (i in numBits + 1..32) {
                    if (nextCodewords[i - 1] == codeword) {
                        nextCodewords[i - 1] = nextCodewords[numBits - 1]
                    }
                }

                /*
                 * Insert this codeword into the 'lookup tree' used for decoding.
                 *
                 * When decoding a codeword, we start at the MSB. We also start at
                 * index 0 of the lookupTree array, which represents the root node.
                 * If the bit is 0, we go 'left' by incrementing the index. If the
                 * bit is 1, we go 'right' by setting the index to the value of
                 * lookupTree[index]. Once we reach the leaf node in the tree,
                 * lookupTree[index] is the complement of the character for the
                 * codeword. The array therefore serves a dual purpose.
                 *
                 * We store the complement of the character rather than the
                 * character itself, as we can then check if lookupTree[node] < 0
                 * to check if we've reached a leaf node yet in decode().
                 */
                var node = 0 // Start at the root node.
                for (i in 1..numBits) {
                    val bit = 1 shl 32 - i
                    if (codeword and bit == 0) {
                        // Go 'left' by incrementing the node number.
                        node++
                    } else {
                        // Allocate a 'right' node if necessary.
                        if (lookupTree[node] == 0) {
                            lookupTree[node] = nextFreeNode
                        }

                        // Go 'right' by setting node := lookupTree[node].
                        node = lookupTree[node]
                    }

                    // Expand lookupTree array if it is too small.
                    if (node >= lookupTree.size) {
                        lookupTree = lookupTree.copyOf(lookupTree.size * 2)
                    }
                }

                // Store (the complement of) the current character in the leaf node.
                lookupTree[node] = chr.inv()

                // Update next free node number.
                if (node >= nextFreeNode) {
                    nextFreeNode = node + 1
                }
            }

            /*
             * The codewords are calculated above such that the codeword starts at
             * the most significant bit of the integer, leaving some trailing zero
             * least significant bits. This code shifts the codewords into the
             * expected position such that the codeword ends at the least
             * significant bit, leaving some unused zero most significant bits.
             *
             * This allows us to directly pass the codeword to the putBits() call
             * in the encode() function.
             */
            for (i in bits.indices) {
                codewords[i] = codewords[i] ushr 32 - bits[i]
            }
            logger.debug {
                "Loaded ${codewords.size} Huffman codewords and ${lookupTree.size} node lookup tree."
            }
            return HuffmanCodec(bits, codewords, lookupTree)
        }
    }
}
