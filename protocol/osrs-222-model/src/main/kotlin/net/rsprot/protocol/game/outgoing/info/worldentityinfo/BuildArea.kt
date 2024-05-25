package net.rsprot.protocol.game.outgoing.info.worldentityinfo

import net.rsprot.protocol.common.game.outgoing.info.CoordGrid
import net.rsprot.protocol.game.outgoing.zone.payload.util.CoordInBuildArea

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
            "ZoneX must be in range of 0..<2048"
        }
        require(zoneZ in 0..<2048) {
            "ZoneZ must be in range of 0..<2048"
        }
        require(widthInZones >= 0) {
            "Width in zones cannot be negative"
        }
        require(heightInZones >= 0) {
            "Height in zones cannot be negative"
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

    internal operator fun contains(avatar: WorldEntityAvatar): Boolean {
        val minBuildAreaZoneX = this.zoneX
        val minBuildAreaZoneZ = this.zoneZ
        val coord = avatar.currentCoord
        val minAvatarZoneX = coord.x ushr 3
        val minAvatarZoneZ = coord.z ushr 3
        if (minAvatarZoneX < minBuildAreaZoneX || minAvatarZoneZ < minBuildAreaZoneZ) {
            return false
        }
        val maxBuildAreaZoneX = minBuildAreaZoneX + this.widthInZones
        val maxBuildAreaZoneZ = minBuildAreaZoneZ + this.heightInZones
        val maxAvatarZoneX = minAvatarZoneX + avatar.sizeX
        val maxAvatarZoneZ = minAvatarZoneZ + avatar.sizeZ
        return !(maxAvatarZoneX > maxBuildAreaZoneX || maxAvatarZoneZ > maxBuildAreaZoneZ)
    }

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

    public companion object {
        public const val DEFAULT_BUILD_AREA_SIZE: Int = 104 ushr 3
        public val INVALID: BuildArea = BuildArea(-1)
    }
}
