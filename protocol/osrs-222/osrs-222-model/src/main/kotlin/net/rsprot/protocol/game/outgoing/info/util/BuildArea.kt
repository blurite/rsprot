package net.rsprot.protocol.game.outgoing.info.util

import net.rsprot.protocol.common.game.outgoing.info.CoordGrid
import net.rsprot.protocol.game.outgoing.zone.payload.util.CoordInBuildArea

/**
 * The build area class is responsible for tracking the currently-rendered
 * map of a given player. Everything sent via world entity info is tracked
 * as relative to the build area.
 * @property zoneX the south-western zone x coordinate of the build area
 * @property zoneZ the south-western zone z coordinate of the build area
 * @property widthInZones the build area width in zones (typically 13, meaning 104 tiles)
 * @property heightInZones the build area height in zones (typically 13, meaning 104 tiles)
 */
@Suppress("MemberVisibilityCanBePrivate")
@JvmInline
public value class BuildArea private constructor(
    private val packed: Long,
) {
    public constructor(
        zoneX: Int,
        zoneZ: Int,
        widthInZones: Int = DEFAULT_BUILD_AREA_SIZE,
        heightInZones: Int = DEFAULT_BUILD_AREA_SIZE,
    ) : this(
        (zoneX and 0xFFFF).toLong()
            .or((zoneZ and 0xFFFF).toLong() shl 16)
            .or((widthInZones and 0xFFFF).toLong() shl 32)
            .or((heightInZones and 0xFFFF).toLong() shl 48),
    ) {
        require(zoneX in 0..<2048) {
            "ZoneX must be in range of 0..<2048: $zoneX"
        }
        require(zoneZ in 0..<2048) {
            "ZoneZ must be in range of 0..<2048: $zoneZ"
        }
        require(widthInZones >= 0) {
            "Width in zones cannot be negative: $widthInZones"
        }
        require(heightInZones >= 0) {
            "Height in zones cannot be negative: $heightInZones"
        }
    }

    public val zoneX: Int
        get() = (packed and 0xFFFF).toInt()
    public val zoneZ: Int
        get() = (packed ushr 16 and 0xFFFF).toInt()
    public val widthInZones: Int
        get() = (packed ushr 32 and 0xFFFF).toInt()
    public val heightInZones: Int
        get() = (packed ushr 48 and 0xFFFF).toInt()

    /**
     * Localizes a specific absolute coordinate to be relative to the south-western
     * corner of this build area.
     * @param coordGrid the coordinate to localize.
     * @return a coordinate local to the build area.
     */
    internal fun localize(coordGrid: CoordGrid): CoordInBuildArea {
        val (_, x, z) = coordGrid
        val buildAreaX = zoneX shl 3
        val buildAreaZ = zoneZ shl 3
        val dx = x - buildAreaX
        val dz = z - buildAreaZ
        val maxDeltaX = this.widthInZones shl 3
        val maxDeltaZ = this.heightInZones shl 3
        check(dx in 0..<maxDeltaX) {
            "Delta x coord out of build area: $this, $coordGrid"
        }
        check(dz in 0..<maxDeltaZ) {
            "Delta z coord out of build area: $this, $coordGrid"
        }
        return CoordInBuildArea(dx, dz)
    }

    /**
     * Checks whether this build area contains the specified coord.
     * Note that this will exclude the 1 tile border of the build area itself.
     * This is due to the client not actually rendering the build area.
     * So given a traditional build area of 104x104 tiles, only the inner 1..102
     * section is rendered - meaning the outer 1 tile border gets excluded.
     * This effect can be best seen here: https://media.z-kris.com/2024/06/javaw_Vw4OYWhkdN.png
     * On the Java client, it seems to cut off one tile border. On the C++ clients, it seems
     * to cut off two. This function will only account for a single tile, as the C++ will still render
     * movement on that other tile fine, even if no map data shows on it.
     * @param coordGrid the coord grid to test.
     * @return whether the coord grid is inside the build area.
     */
    internal operator fun contains(coordGrid: CoordGrid): Boolean {
        val (_, x, z) = coordGrid
        val buildAreaX = zoneX shl 3
        val dx = x - buildAreaX
        if (dx < 1) {
            return false
        }
        val buildAreaZ = zoneZ shl 3
        val dz = z - buildAreaZ
        if (dz < 1) {
            return false
        }
        val maxDeltaX = this.widthInZones shl 3
        if (dx >= (maxDeltaX - 1)) {
            return false
        }
        val maxDeltaZ = this.heightInZones shl 3
        return dz < (maxDeltaZ - 1)
    }

    public companion object {
        /**
         * The default build area size in zones.
         */
        public const val DEFAULT_BUILD_AREA_SIZE: Int = 104 ushr 3

        /**
         * An uninitialized build area.
         */
        public val INVALID: BuildArea = BuildArea(-1)
    }
}
