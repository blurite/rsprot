package net.rsprot.protocol.api.obfuscation

import net.rsprot.protocol.game.incoming.prot.GameClientProt
import net.rsprot.protocol.game.outgoing.prot.GameServerProt
import java.util.SplittableRandom

/**
 * An opcode mapper implementation, allowing for simple obfuscation of opcodes.
 * @property sourceOpcodes the original input opcodes (ones found in client by default)
 * @property obfuscatedOpcodes the modified opcodes for obfuscation
 */
@OptIn(ExperimentalUnsignedTypes::class)
public class OpcodeMapper private constructor(
    @PublishedApi
    internal val sourceOpcodes: UByteArray,
    @PublishedApi
    internal val obfuscatedOpcodes: UByteArray,
) {
    @Suppress("NOTHING_TO_INLINE")
    public inline fun encode(opcode: Int): Int {
        return sourceOpcodes[opcode].toInt()
    }

    @Suppress("NOTHING_TO_INLINE")
    public inline fun decode(opcode: Int): Int {
        return obfuscatedOpcodes[opcode].toInt()
    }

    /**
     * Builds a source -> obfuscated hashmap of the opcodes.
     */
    public fun toMap(): Map<Int, Int> {
        val source = sourceOpcodes.map { it.toInt() }
        val obfuscated = obfuscatedOpcodes.map { it.toInt() }
        return (source zip obfuscated).toMap()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as OpcodeMapper

        if (!sourceOpcodes.contentEquals(other.sourceOpcodes)) return false
        if (!obfuscatedOpcodes.contentEquals(other.obfuscatedOpcodes)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = sourceOpcodes.contentHashCode()
        result = 31 * result + obfuscatedOpcodes.contentHashCode()
        return result
    }

    override fun toString(): String {
        return "OpcodeMapper(" +
            "sourceOpcodes=${sourceOpcodes.contentToString()}, " +
            "obfuscatedOpcodes=${obfuscatedOpcodes.contentToString()}" +
            ")"
    }

    public companion object {
        /**
         * Builds an opcode mapper of input and output opcodes, with no modifications applied.
         * @param inputOpcodes the real opcodes used
         * @param outputOpcodes the obfuscated opcodes used
         * @return an instance of an opcode mapper.
         */
        @JvmStatic
        public fun of(
            inputOpcodes: ByteArray,
            outputOpcodes: ByteArray,
        ): OpcodeMapper {
            return OpcodeMapper(
                inputOpcodes.toUByteArray(),
                outputOpcodes.toUByteArray(),
            )
        }

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
            return fromSeed(seed, opcodes)
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
            return fromSeed(seed, opcodes)
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
        public fun fromSeed(
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
            val dec = UByteArray(count)
            for (x in 0 until count) {
                val y = permutated[x]
                enc[x] = y.toUByte()
                dec[y] = x.toUByte()
            }

            return OpcodeMapper(enc, dec)
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
