package net.rsprot.protocol.game.outgoing.info.worldentityinfo

import it.unimi.dsi.fastutil.ints.Int2IntMap
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap
import net.rsprot.protocol.internal.game.outgoing.info.CoordGrid

/**
 * A primitive hash map implementation to store which world entity owns a specific zones.
 * This map will track every zone used up by the instance, so we can do simple [CoordGrid] -> [ZoneCoord]
 * conversions and look up the respective world entity index that owns it, if any.
 * @property map the primitive hashmap keeping track of <bitpacked zone id, world entity index>.
 */
internal class WorldEntityMap(
    initialCapacity: Int = 10_000,
) {
    private val map: Int2IntMap =
        Int2IntOpenHashMap(initialCapacity).apply {
            defaultReturnValue(-1)
        }

    /**
     * Tries to store the world entity with [index] in the provided rectangle of zones.
     * If an exception is thrown while marking the rectangle, anything that was marked will be unmarked
     * before the exception is re-thrown.
     * @param southWestZoneX the zone x coordinate of the south-westernmost zone in the instance.
     * @param southWestZoneZ the zone z coordinate of the south-westernmost zone in the instance.
     * @param widthInZones the width of the instance in zones.
     * A value of 1 means the instance has a width of 8 tiles.
     * @param lengthInZones the length of the instance in zones.
     * A value of 1 means the instance has a length of 8 tiles.
     * @param minLevel the minimum level occupied by the instance.
     * @param maxLevel the maximum level occupied by the instance. This is inclusive.
     * Note that it is not possible to make an instance which skips a level, as any such instance is illogical.
     * @param index the index of the world entity that owns this slab of land.
     */
    fun put(
        southWestZoneX: Int,
        southWestZoneZ: Int,
        widthInZones: Int,
        lengthInZones: Int,
        minLevel: Int,
        maxLevel: Int,
        index: Int,
    ) {
        try {
            fillRectangle(
                southWestZoneX,
                southWestZoneZ,
                widthInZones,
                lengthInZones,
                minLevel,
                maxLevel,
                index,
            )
        } catch (e: Throwable) {
            // Clean up any that were partially filled with the provided index
            clearRectangle(
                southWestZoneX,
                southWestZoneZ,
                widthInZones,
                lengthInZones,
                minLevel,
                maxLevel,
                index,
            )

            // Re-throw the old exception to let the caller handle it
            throw e
        }
    }

    /**
     * Removes the world entity with [expectedIndex] in the provided rectangle of zones.
     * @param southWestZoneX the zone x coordinate of the south-westernmost zone in the instance.
     * @param southWestZoneZ the zone z coordinate of the south-westernmost zone in the instance.
     * @param widthInZones the width of the instance in zones.
     * A value of 1 means the instance has a width of 8 tiles.
     * @param lengthInZones the length of the instance in zones.
     * A value of 1 means the instance has a length of 8 tiles.
     * @param minLevel the minimum level occupied by the instance.
     * @param maxLevel the maximum level occupied by the instance. This is inclusive.
     * Note that it is not possible to make an instance which skips a level, as any such instance is illogical.
     */
    fun remove(
        southWestZoneX: Int,
        southWestZoneZ: Int,
        widthInZones: Int,
        lengthInZones: Int,
        minLevel: Int,
        maxLevel: Int,
        expectedIndex: Int,
    ) {
        clearRectangle(
            southWestZoneX,
            southWestZoneZ,
            widthInZones,
            lengthInZones,
            minLevel,
            maxLevel,
            expectedIndex,
        )
    }

    /**
     * Gets the world entity index that owns the zone that the [coordGrid] belongs in, or -1 if none is present.
     * @return world entity id that owns the zone, or -1.
     */
    fun get(coordGrid: CoordGrid): Int {
        return map.get(coordGrid.toZoneCoord().packed)
    }

    /**
     * Fills the land with [index].
     * @param southWestZoneX the zone x coordinate of the south-westernmost zone in the instance.
     * @param southWestZoneZ the zone z coordinate of the south-westernmost zone in the instance.
     * @param widthInZones the width of the instance in zones.
     * A value of 1 means the instance has a width of 8 tiles.
     * @param lengthInZones the length of the instance in zones.
     * A value of 1 means the instance has a length of 8 tiles.
     * @param minLevel the minimum level occupied by the instance.
     * @param maxLevel the maximum level occupied by the instance. This is inclusive.
     * Note that it is not possible to make an instance which skips a level, as any such instance is illogical.
     * @param index the index of the world entity that owns this slab of land.
     */
    private fun fillRectangle(
        southWestZoneX: Int,
        southWestZoneZ: Int,
        widthInZones: Int,
        lengthInZones: Int,
        minLevel: Int,
        maxLevel: Int,
        index: Int,
    ) {
        for (level in minLevel..maxLevel) {
            for (offsetXInZones in 0..<widthInZones) {
                for (offsetZInZones in 0..<lengthInZones) {
                    val zoneCoord =
                        ZoneCoord(
                            level = level,
                            zoneX = southWestZoneX + offsetXInZones,
                            zoneZ = southWestZoneZ + offsetZInZones,
                        )
                    val old =
                        map.putIfAbsent(
                            zoneCoord.packed,
                            index,
                        )
                    if (old != -1) {
                        throw IllegalStateException("World entity already exists at $zoneCoord")
                    }
                }
            }
        }
    }

    /**
     * Clears the land with [index].
     * @param southWestZoneX the zone x coordinate of the south-westernmost zone in the instance.
     * @param southWestZoneZ the zone z coordinate of the south-westernmost zone in the instance.
     * @param widthInZones the width of the instance in zones.
     * A value of 1 means the instance has a width of 8 tiles.
     * @param lengthInZones the length of the instance in zones.
     * A value of 1 means the instance has a length of 8 tiles.
     * @param minLevel the minimum level occupied by the instance.
     * @param maxLevel the maximum level occupied by the instance. This is inclusive.
     * Note that it is not possible to make an instance which skips a level, as any such instance is illogical.
     * @param index the index of the world entity that owns this slab of land.
     */
    private fun clearRectangle(
        southWestZoneX: Int,
        southWestZoneZ: Int,
        widthInZones: Int,
        lengthInZones: Int,
        minLevel: Int,
        maxLevel: Int,
        index: Int,
    ) {
        for (level in minLevel..maxLevel) {
            for (offsetXInZones in 0..<widthInZones) {
                for (offsetZInZones in 0..<lengthInZones) {
                    val zoneCoord =
                        ZoneCoord(
                            level = level,
                            zoneX = southWestZoneX + offsetXInZones,
                            zoneZ = southWestZoneZ + offsetZInZones,
                        )
                    val existing = map.get(zoneCoord.packed)
                    if (existing == index) {
                        map.remove(zoneCoord.packed)
                    }
                }
            }
        }
    }

    /**
     * Converts a [CoordGrid] into a [ZoneCoord] by dropping the 3 least significant bits on the x and z axis.
     * @return a zone coord representing the zone which owns the input [CoordGrid].
     */
    private fun CoordGrid.toZoneCoord(): ZoneCoord {
        return ZoneCoord(
            level = this.level,
            zoneX = this.x ushr 3,
            zoneZ = this.z ushr 3,
        )
    }

    /**
     * A value class to bitpack a zone's coordinate into a single integer, for efficient storage.
     * @property packed the bitpacked zone coordinate.
     * @property zoneX the zone x coordinate. Multiply by 8 to get the absolute coordinate.
     * @property zoneX the zone z coordinate. Multiply by 8 to get the absolute coordinate.
     * @property level the level of the zone.
     */
    @JvmInline
    private value class ZoneCoord(
        val packed: Int,
    ) {
        constructor(
            level: Int,
            zoneX: Int,
            zoneZ: Int,
        ) : this(
            (level shl 22).or(zoneX shl 11).or(zoneZ),
        ) {
            require(level in 0..<4) {
                "Level must be in range of 0..<4"
            }
            require(zoneX in 0..<2048) {
                "Zone x must be in range of 0..<2048"
            }
            require(zoneZ in 0..<2048) {
                "Zone z must be in range of 0..<2048"
            }
        }

        val zoneX: Int
            get() = packed ushr 11 and 0x7FF
        val zoneZ: Int
            get() = packed and 0x7FF
        val level: Int
            get() = packed ushr 22 and 0x3

        override fun toString(): String {
            return "ZoneCoord(" +
                "zoneX=$zoneX, " +
                "zoneZ=$zoneZ, " +
                "level=$level" +
                ")"
        }
    }
}
