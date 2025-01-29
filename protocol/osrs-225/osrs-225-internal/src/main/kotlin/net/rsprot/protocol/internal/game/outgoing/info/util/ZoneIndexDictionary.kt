package net.rsprot.protocol.internal.game.outgoing.info.util

/**
 * A specialized dictionary mapping int keys to [ZoneIndexArray] values.
 * This implementation is a stripped down variant of
 * [FastUtil's Int2ObjetOpenHashMap](https://fastutil.di.unimi.it/docs/it/unimi/dsi/fastutil/ints/Int2ObjectOpenHashMap.html)
 *
 * This implementation strips away any resizing logic, generic overhead and most of the functions,
 * only containing the handful that our implementation requires.
 *
 * @param maxKeyCount the maximum number of keys that will be stored in the map. This value must be
 * a power of two (e.g. 2048, 65536).
 * @property capacity the capacity with which we allocate our arrays. We currently use 4x larger
 * arrays to back our hashmap to bring down the load factor of our hashmap and reduce the collisions.
 * When the hashmap is fully filled up with valid keys, it will only be ~25% filled as far as the arrays go.
 * @property mask the bitmask that is used to mix the keys.
 * @property containsNullKey whether the map contains a zero-key. Due to this being used throughout
 * the implementations, I'm leaving it in as to avoid potential mistakes and edge-case bugs. Note that not
 * every reference to the [containsNullKey] is necessarily a use case of it, some constant 0 values here
 * relate to the null key as well.
 * @property keys an array of hashmap keys mixed with the [mask].
 * @property values an array of zone index arrays, containing the NPC or WorldEntity indices that
 * we intend to store.
 */
@Suppress("DuplicatedCode")
internal class ZoneIndexDictionary(
    maxKeyCount: Int,
) {
    private val capacity: Int = maxKeyCount * 4
    private val mask: Int = capacity - 1
    private var containsNullKey: Boolean = false
    private val keys: IntArray = IntArray(capacity)
    private val values: Array<net.rsprot.protocol.internal.game.outgoing.info.util.ZoneIndexArray?> = arrayOfNulls(capacity)

    /**
     * Puts a [value] into this dictionary with the specified [key].
     */
    fun put(
	    key: Int,
	    value: net.rsprot.protocol.internal.game.outgoing.info.util.ZoneIndexArray,
    ) {
        val pos = find(key)
        if (pos < 0) {
            insert(-pos - 1, key, value)
            return
        }
        values[pos] = value
    }

    /**
     * Inserts a value into our hashmap with the specified [key] and [value] at the [pos].
     */
    private fun insert(
	    pos: Int,
	    key: Int,
	    value: net.rsprot.protocol.internal.game.outgoing.info.util.ZoneIndexArray,
    ) {
        if (pos == mask) {
            containsNullKey = true
        }
        keys[pos] = key
        values[pos] = value
    }

    /**
     * Finds the respective key [k] in our dictionary. If the key does not exist,
     * a negative position value is returned where the key should be inserted.
     * @param k the key to find
     * @return the index at which the key [k] is stored, or a negative value determining
     * where the key should be put.
     */
    private fun find(k: Int): Int {
        if (k == 0) {
            return if (containsNullKey) {
                mask
            } else {
                -(mask + 1)
            }
        }
        var pos: Int = k and mask
        val key = this.keys
        var curr: Int = key[pos]
        if (curr == 0) {
            return -(pos + 1)
        }
        if (k == curr) {
            return pos
        }
        while (true) {
            pos = (pos + 1) and mask
            curr = key[pos]
            if (curr == 0) {
                return -(pos + 1)
            }
            if (k == curr) {
                return pos
            }
        }
    }

    /**
     * Removes the key-value pair associated with [k], if it exists.
     */
    fun remove(k: Int) {
        if (k == 0) {
            if (containsNullKey) {
                removeNullEntry()
            }
            return
        }
        var key = this.keys
        var pos: Int = k and mask
        var curr: Int = key[pos]
        if (curr == 0) {
            return
        }
        if (k == curr) {
            removeEntry(pos)
            return
        }
        while (true) {
            pos = (pos + 1) and mask
            curr = key[pos]
            if (curr == 0) {
                return
            }
            if (k == curr) {
                removeEntry(pos)
                return
            }
        }
    }

    /**
     * Gets the zone index array associated with the key [k] if it exists, or null.
     */
    fun get(k: Int): net.rsprot.protocol.internal.game.outgoing.info.util.ZoneIndexArray? {
        if (k == 0) {
            return if (containsNullKey) {
                values[mask]
            } else {
                null
            }
        }
        var key = this.keys
        var pos: Int = k and mask
        var curr: Int = key[pos]
        if (curr == 0) {
            return null
        }
        if (k == curr) {
            return values[pos]
        }
        while (true) {
            pos = (pos + 1) and mask
            curr = key[pos]
            if (curr == 0) {
                return null
            }
            if (k == curr) {
                return values[pos]
            }
        }
    }

    /**
     * Removes the zero-key entry from this hashmap.
     */
    private fun removeNullEntry() {
        containsNullKey = false
        values[mask] = null
    }

    /**
     * Removes the entry at [pos] in this hashmap.
     */
    private fun removeEntry(pos: Int) {
        values[pos] = null
    }
}
