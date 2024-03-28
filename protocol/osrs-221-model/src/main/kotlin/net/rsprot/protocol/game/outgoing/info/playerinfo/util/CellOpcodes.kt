package net.rsprot.protocol.game.outgoing.info.playerinfo.util

internal object CellOpcodes {
    // Single cell opcodes
    private const val SW: Int = 0
    private const val S: Int = 1
    private const val SE: Int = 2
    private const val W: Int = 3
    private const val E: Int = 4
    private const val NW: Int = 5
    private const val N: Int = 6
    private const val NE: Int = 7

    // Dual cell opcodes (each letter stands for 1 cell in that direction, SSWW = 2 tiles south, 2 tiles west)
    private const val SSWW: Int = 0
    private const val SSW: Int = 1
    private const val SS: Int = 2
    private const val SSE: Int = 3
    private const val SSEE: Int = 4
    private const val SWW: Int = 5
    private const val SEE: Int = 6
    private const val WW: Int = 7
    private const val EE: Int = 8
    private const val NWW: Int = 9
    private const val NEE: Int = 10
    private const val NNWW: Int = 11
    private const val NNW: Int = 12
    private const val NN: Int = 13
    private const val NNE: Int = 14
    private const val NNEE: Int = 15
    private val singleCellMovementOpcodes: IntArray = buildSingleCellMovementOpcodes()
    private val dualCellMovementOpcodes: IntArray = buildDualCellMovementOpcodes()

    private fun singleCellIndex(
        deltaX: Int,
        deltaZ: Int,
    ): Int {
        return (deltaX + 1).or((deltaZ + 1) shl 2)
    }

    private fun dualCellIndex(
        deltaX: Int,
        deltaZ: Int,
    ): Int {
        return (deltaX + 2).or((deltaZ + 2) shl 3)
    }

    internal fun singleCellMovementOpcode(
        deltaX: Int,
        deltaZ: Int,
    ): Int {
        return singleCellMovementOpcodes[singleCellIndex(deltaX, deltaZ)]
    }

    internal fun dualCellMovementOpcode(
        deltaX: Int,
        deltaZ: Int,
    ): Int {
        return dualCellMovementOpcodes[dualCellIndex(deltaX, deltaZ)]
    }

    /**
     * Builds a simple bitpacked array of the bit codes for all the possible deltas.
     * This is simply a more efficient variant of the normal if-else chain of checking
     * the different delta combinations, as we are skipping a lot of branch prediction.
     * In a benchmark, the results showed ~603% increased performance.
     */
    private fun buildSingleCellMovementOpcodes(): IntArray {
        val array = IntArray(16) { -1 }
        array[singleCellIndex(-1, -1)] = SW
        array[singleCellIndex(0, -1)] = S
        array[singleCellIndex(1, -1)] = SE
        array[singleCellIndex(-1, 0)] = W
        array[singleCellIndex(1, 0)] = E
        array[singleCellIndex(-1, 1)] = NW
        array[singleCellIndex(0, 1)] = N
        array[singleCellIndex(1, 1)] = NE
        return array
    }

    /**
     * Similarly to [buildSingleCellMovementOpcodes], this is significantly more efficient
     * than chained if-else statements.
     * In this case, as there are more branches, the benchmark showed a 891% performance increase.
     * It is worth noting that the benchmark in question also included reading deltas from
     * a pre-computed array and thus, the real gain would actually be even more significant if only
     * comparing the raw time taken by reading the opcode alone.
     */
    private fun buildDualCellMovementOpcodes(): IntArray {
        val array = IntArray(64) { -1 }
        array[dualCellIndex(-2, -2)] = SSWW
        array[dualCellIndex(-1, -2)] = SSW
        array[dualCellIndex(0, -2)] = SS
        array[dualCellIndex(1, -2)] = SSE
        array[dualCellIndex(2, -2)] = SSEE
        array[dualCellIndex(-2, -1)] = SWW
        array[dualCellIndex(2, -1)] = SEE
        array[dualCellIndex(-2, 0)] = WW
        array[dualCellIndex(2, 0)] = EE
        array[dualCellIndex(-2, 1)] = NWW
        array[dualCellIndex(2, 1)] = NEE
        array[dualCellIndex(-2, 2)] = NNWW
        array[dualCellIndex(-1, 2)] = NNW
        array[dualCellIndex(0, 2)] = NN
        array[dualCellIndex(1, 2)] = NNE
        array[dualCellIndex(2, 2)] = NNEE
        return array
    }
}
