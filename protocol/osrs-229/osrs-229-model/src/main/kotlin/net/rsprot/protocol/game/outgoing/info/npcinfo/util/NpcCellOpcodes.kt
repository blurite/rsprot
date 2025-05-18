@file:Suppress("DuplicatedCode")

package net.rsprot.protocol.game.outgoing.info.npcinfo.util

internal object NpcCellOpcodes {
    private const val NW: Int = 0
    private const val N: Int = 1
    private const val NE: Int = 2
    private const val W: Int = 3
    private const val E: Int = 4
    private const val SW: Int = 5
    private const val S: Int = 6
    private const val SE: Int = 7

    /**
     * Single cell movement opcodes in a len-16 array.
     */
    private val singleCellMovementOpcodes: IntArray = buildSingleCellMovementOpcodes()

    /**
     * Gets the index for a single cell movement opcode based on the deltas,
     * where the deltas are expected to be either -1, 0 or 1.
     * @param deltaX the x-coordinate delta
     * @param deltaZ the z-coordinate delta
     * @return the index of the single cell opcode stored in [singleCellMovementOpcodes]
     */
    private fun singleCellIndex(
        deltaX: Int,
        deltaZ: Int,
    ): Int = (deltaX + 1).or((deltaZ + 1) shl 2)

    /**
     * Gets the single cell movement opcode value for the provided deltas.
     * @param deltaX the x-coordinate delta
     * @param deltaZ the z-coordinate delta
     * @return the movement opcode as expected by the client, or -1 if the deltas are in range,
     * but the deltas do not result in any movement.
     * @throws ArrayIndexOutOfBoundsException if either of the deltas is not in range of -1..1.
     */
    @Throws(ArrayIndexOutOfBoundsException::class)
    internal fun singleCellMovementOpcode(
        deltaX: Int,
        deltaZ: Int,
    ): Int = singleCellMovementOpcodes[singleCellIndex(deltaX, deltaZ)]

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
}
