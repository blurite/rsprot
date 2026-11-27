package net.rsprot.protocol.api.obfuscation

import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import java.util.SplittableRandom

/**
 * An opcode mapper implementation, allowing for simple obfuscation of opcodes.
 * @property opcodes the array of opcodes, going in either direction (conditional).
 *
 * For server-to-client, this builds an array
 * where the index is the source opcode and the value is the obfuscated opcode.
 *
 * For client-to-server, this builds an array where the index of the obfuscated opcode and the
 * value is the source opcode.
 */
@OptIn(ExperimentalUnsignedTypes::class)
public class OpcodeMapper private constructor(
    @PublishedApi
    internal val opcodes: UByteArray,
) {
    public val length: Int
        get() = opcodes.size

    @Suppress("NOTHING_TO_INLINE")
    public inline fun map(opcode: Int): Int {
        return opcodes[opcode].toInt()
    }

    /**
     * Builds an inverted array for client-sided use.
     *
     * The **index** in the array will be the value that client has available:
     * - For client-to-server packets in client, this is the original opcode.
     * - For server-to-client packets in client, this is the obfuscated opcode.
     *
     * The **value** from the array corresponds to the inverse:
     * - For client-to-server packets in client, this is the obfuscated opcode.
     * - For server-to-client packets in client, this is the original opcode.
     */
    public fun toInvertedIntArray(): IntArray {
        val arr = IntArray(length)
        for (src in arr.indices) {
            val dest = opcodes[src].toInt()
            arr[dest] = src
        }
        return arr
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as OpcodeMapper

        return opcodes.contentEquals(other.opcodes)
    }

    override fun hashCode(): Int {
        return opcodes.contentHashCode()
    }

    override fun toString(): String {
        return "OpcodeMapper(" +
            "mappedOpcodes=${opcodes.contentToString()}" +
            ")"
    }

    public companion object {
        /**
         * Builds an opcode mapper for server-to-client packets out of the provided [seed].
         */
        @JvmStatic
        public fun seededServerProtMapper(seed: Long): OpcodeMapper {
            val opcodes =
                GameServerProt.entries
                    .filter { it.opcode >= 0 }
                    .map { it.opcode }
                    .toIntArray()
            return fromSeedEncoding(seed, opcodes)
        }

        /**
         * Builds an opcode mapper for client-to-server packets out of the provided [seed].
         */
        @JvmStatic
        public fun seededClientProtMapper(seed: Long): OpcodeMapper {
            val opcodes =
                GameClientProt.entries
                    .filter { it.opcode >= 0 }
                    .map { it.opcode }
                    .toIntArray()
            return fromSeedDecoding(seed, opcodes)
        }

        /**
         * Builds an opcode mapper from a specific seed, for an int array of opcodes.
         * @param seed the input seed to use. The same seed will always generate the same
         * values for a given array of [inputs].
         * @param inputs the source opcodes to remap.
         * @throws IllegalArgumentException if inputs don't meet our requirements
         * (consecutive values of 0..n with no gaps)
         * @return an instance of an opcode mapper.
         */
        @JvmStatic
        public fun fromSeedEncoding(
            seed: Long,
            inputs: IntArray,
        ): OpcodeMapper {
            validate(inputs)

            val count = inputs.size
            // Fisher–Yates permutation
            val permutated = IntArray(count) { it }
            val random = SplittableRandom(seed)
            for (index in count - 1 downTo 1) {
                val obfuscated = random.nextInt(index + 1)
                val original = permutated[index]
                permutated[index] = permutated[obfuscated]
                permutated[obfuscated] = original
            }

            // Compress the data to an unsigned byte array for better memory footprint
            // which also makes it more likely to get inlined by JIT.
            val enc = UByteArray(count)
            for (x in 0 until count) {
                val y = permutated[x]
                enc[x] = y.toUByte()
            }

            return OpcodeMapper(enc)
        }

        /**
         * Builds an opcode mapper from a specific seed, for an int array of opcodes.
         * @param seed the input seed to use. The same seed will always generate the same
         * values for a given array of [inputs].
         * @param inputs the source opcodes to remap.
         * @throws IllegalArgumentException if inputs don't meet our requirements
         * (consecutive values of 0..n with no gaps)
         * @return an instance of an opcode mapper.
         */
        @JvmStatic
        public fun fromSeedDecoding(
            seed: Long,
            inputs: IntArray,
        ): OpcodeMapper {
            validate(inputs)

            val count = inputs.size
            // Fisher–Yates permutation
            val permutated = IntArray(count) { it }
            val random = SplittableRandom(seed)
            for (index in count - 1 downTo 1) {
                val obfuscated = random.nextInt(index + 1)
                val original = permutated[index]
                permutated[index] = permutated[obfuscated]
                permutated[obfuscated] = original
            }

            // Compress the data to an unsigned byte array for better memory footprint
            // which also makes it more likely to get inlined by JIT.
            val dec = UByteArray(count)
            for (x in 0 until count) {
                val y = permutated[x]
                dec[y] = x.toUByte()
            }

            return OpcodeMapper(dec)
        }

        /**
         * Validates the [inputs] opcodes array to ensure it has consecutive values from 0..n,
         * and has no duplicates.
         * @param inputs the inputs to validate
         * @throws IllegalArgumentException if inputs don't meet our requirements.
         */
        private fun validate(inputs: IntArray) {
            val count = inputs.size
            require(count in 1..256) {
                "Inputs size must be 1..256"
            }
            val seen = BooleanArray(count)
            var max = -1
            for (opcode in inputs) {
                require(opcode in 0 until count) {
                    "Inputs must be 0..${count - 1} with no gaps; found $opcode"
                }
                require(!seen[opcode]) {
                    "Duplicate input opcode: $opcode"
                }
                seen[opcode] = true
                if (opcode > max) {
                    max = opcode
                }
            }
            require(max == count - 1) { "inputs must cover 0..${count - 1} exactly" }
        }
    }
}
