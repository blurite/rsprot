package net.rsprot.protocol.internal.game.outgoing.info.util

import net.rsprot.protocol.internal.game.outgoing.info.CoordGrid

/**
 * A dictionary based implementation allowing one to store npc indices within zones.
 * @param maxKeyCount the maximum number of keys that will be stored in the map. This value must be
 * a power of two (e.g. 2048, 65536).
 * @property dictionary a stripped down primitive hashmap implementation that stores zone objects based
 * on their bitpacked coordinate.
 */
@Suppress("DuplicatedCode")
public class ZoneIndexStorage(
    maxKeyCount: Int,
) {
    private val dictionary = ZoneIndexDictionary(maxKeyCount)

    /**
     * Adds the [entityIndex] to the zone that contains the [coordGrid] coord.
     * @param entityIndex the index to add to the end of the zone.
     * @param coordGrid the coord grid at which to find the zone array.
     * If the zone does not exist, a new instance is created.
     */
    public fun add(
	    entityIndex: Int,
	    coordGrid: net.rsprot.protocol.internal.game.outgoing.info.CoordGrid,
    ) {
        val zoneIndex = zoneIndex(coordGrid)
        var array = dictionary.get(zoneIndex)
        if (array == null) {
            array = ZoneIndexArray()
            array.add(entityIndex)
            dictionary.put(zoneIndex, array)
            return
        }
        array.add(entityIndex)
    }

    /**
     * Removes the [entityIndex] at the zone located at [coordGrid].
     * @param entityIndex the index to remove from the zone.
     * @param coordGrid the coord grid at which the zone exists.
     * If there's only one index remaining in the respective zone index array,
     * the zone index array will be disposed.
     */
    public fun remove(
	    entityIndex: Int,
	    coordGrid: net.rsprot.protocol.internal.game.outgoing.info.CoordGrid,
    ) {
        val zoneIndex = zoneIndex(coordGrid)
        val array =
            checkNotNull(dictionary.get(zoneIndex)) {
                "Array not found at $coordGrid"
            }
        if (array.size == 1) {
            dictionary.remove(zoneIndex)
            return
        }
        array.remove(entityIndex)
    }

    /**
     * Gets the array of indices at the provided zone coordinate (not to be confused with
     * coord grids).
     * @param level the level at which the zone is
     * @param zoneX the zone x coordinate, obtained via coordGrid.x shr 3
     * @param zoneZ the zone z coordinate, obtained via coordGrid.z shr 3
     * @return a short array containing the indices of all the elements in the array.
     * The array may contain 65535 values at the end, which should be ignored as they
     * imply an open spot for future additions.
     */
    public fun get(
        level: Int,
        zoneX: Int,
        zoneZ: Int,
    ): ShortArray? {
        val zoneIndex =
            (level shl 22)
                .or(zoneX shl 11)
                .or(zoneZ)
        return dictionary.get(zoneIndex)?.array
    }

    /**
     * Bitpacks the zone index based on the [coordGrid].
     * @param coordGrid the coord grid from which to calculate the zone index.
     * @return the bitpacked zone index.
     */
    private fun zoneIndex(coordGrid: net.rsprot.protocol.internal.game.outgoing.info.CoordGrid): Int {
        val level = coordGrid.level
        val zoneX = coordGrid.x ushr 3
        val zoneZ = coordGrid.z ushr 3
        return (level shl 22)
            .or(zoneX shl 11)
            .or(zoneZ)
    }

    public companion object {
        public const val WORLDENTITY_CAPACITY: Int = 2048
        public const val NPC_CAPACITY: Int = 65536
    }
}
