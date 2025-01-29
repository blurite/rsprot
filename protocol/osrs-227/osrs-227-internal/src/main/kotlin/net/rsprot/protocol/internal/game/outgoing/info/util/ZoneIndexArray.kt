package net.rsprot.protocol.internal.game.outgoing.info.util

/**
 * A super-unsafe internal storage class for NPCs and WorldEntities.
 *
 * This class is only intended to be used within the RSProt library, as the functions
 * it offers do not perform any validation and rely on correctness of use by the
 * library itself. This is done in order to improve performance.
 * @property array the array of indices. This array will only ever grow in size, never shrink.
 * Whenever elements are removed, their value is overwritten with [FREE_INDEX].
 * The respective [ZoneIndexStorage] will delete this entire object if the index count in here
 * falls to zero, in order to avoid memory growing indefinitely.
 * @property size the current number of elements in the array.
 */
internal class ZoneIndexArray(
    var array: ShortArray,
    var size: Int,
) {
    constructor() : this(
        ShortArray(2) {
            FREE_INDEX
        },
        0,
    )

    /**
     * Adds the [index] to the array. If the array is full, it gets doubled in size before
     * the entry is added in.
     * @param index the index to add to the array, must be a value in range of 0..65534.
     */
    fun add(index: Int) {
        // If our array is full, double the capacity
        if (array.size == size) {
            val copy =
                ShortArray(array.size * 2) {
                    FREE_INDEX
                }
            array.copyInto(copy)
            this.array = copy
        }
        array[size++] = index.toShort()
    }

    /**
     * Removes the [index] from the array, or throws an [IllegalArgumentException] if it does not exist.
     * @param index the index to remove from this array.
     * @throws IllegalArgumentException if the index is not within the array.
     */
    fun remove(index: Int) {
        val indexAsShort = index.toShort()
        for (i in 0..<size) {
            if (indexAsShort == array[i]) {
                array.copyInto(array, i, i + 1, size--)
                return
            }
        }
        throw IllegalArgumentException("Index not in zone array: $index")
    }

    private companion object {
        private const val FREE_INDEX: Short = -1
    }
}
