package net.rsprot.protocol.game.outgoing.info.util

/**
 * A class that keeps the top-k results offered to it.
 * When the array is full, the lowest result is evicted if that result is a lower weight
 * than what is currently being added.
 * @param maxResults the maximum number of results to keep.
 * @property indices the indices of the elements that are kept.
 * @property weights the weights of the elements that are kept.
 * @property size the number of elements currently in the array.
 * @property worstIndex the index of the worst element for a quick comparison/eviction.
 * @property worstWeight the weight of the worst element for quick comparison/eviction.
 */
internal class UnsortedTopKArray(
    maxResults: Int,
) {
    val indices = IntArray(maxResults)
    private val weights = LongArray(maxResults)
    var size: Int = 0
        private set

    private var worstIndex: Int = -1
    private var worstWeight: Long = Long.MAX_VALUE
    private var maxSize: Int = indices.size

    /**
     * Resets the search results, allowing for a clean slate.
     */
    fun reset(maxSize: Int = indices.size) {
        require(maxSize in 0..indices.size)
        this.maxSize = maxSize
        size = 0
        worstIndex = -1
        worstWeight = Long.MAX_VALUE
    }

    /**
     * Checks if this array contains the provided [index].
     * @return true if the index is contained in the array.
     */
    fun contains(index: Int): Boolean {
        for (i in 0..<size) {
            if (indices[i] == index) {
                return true
            }
        }
        return false
    }

    /**
     * Trims the collection to [maxSize] if it exceeds the size, by evicting worst-weight entities.
     */
    fun trimToSize(maxSize: Int) {
        require(maxSize in 0..indices.size) {
            "Max size out of boundaries of 0..${indices.size}: $maxSize"
        }
        this.maxSize = maxSize
        while (size > maxSize) {
            size--
            indices[worstIndex] = indices[size]
            weights[worstIndex] = weights[size]
            findWorst()
        }
    }

    /**
     * Insert a candidate if it belongs in the top-K set.
     * @param index entity index to offer
     * @param weight value, higher is better, lower is evicted if full.
     */
    fun offer(
        index: Int,
        weight: Long,
    ) {
        if (size < maxSize) {
            val idx = size
            indices[idx] = index
            weights[idx] = weight
            size++

            if (worstIndex == -1 || weight < worstWeight) {
                worstIndex = idx
                worstWeight = weight
            }
            return
        }

        if (weight <= worstWeight) {
            return
        }

        val idx = worstIndex
        indices[idx] = index
        weights[idx] = weight

        findWorst()
    }

    private fun findWorst() {
        if (size == 0) {
            worstIndex = -1
            worstWeight = Long.MAX_VALUE
            return
        }

        var wi = 0
        var wk = weights[0]
        var i = 1
        while (i < size) {
            val ki = weights[i]
            if (ki < wk) {
                wk = ki
                wi = i
            }
            i++
        }
        worstIndex = wi
        worstWeight = wk
    }
}
