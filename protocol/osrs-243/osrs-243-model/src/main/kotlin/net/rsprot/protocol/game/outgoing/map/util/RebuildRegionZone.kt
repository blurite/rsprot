package net.rsprot.protocol.game.outgoing.map.util

/**
 * This class wraps a reference zone to be copied together with the respective
 * xtea key needed to decrypt the backing mapsquare.
 * @property referenceZone the zone to be copied from the static map
 */
public class RebuildRegionZone public constructor(
    public val referenceZone: ReferenceZone,
) {
    public constructor(
        zoneX: Int,
        zoneZ: Int,
        level: Int,
        rotation: Int,
    ) : this(
        ReferenceZone(
            zoneX,
            zoneZ,
            level,
            rotation,
        ),
    )

    public val rotation: Int
        get() = referenceZone.rotation
    public val zoneX: Int
        get() = referenceZone.zoneX
    public val zoneZ: Int
        get() = referenceZone.zoneZ
    public val level: Int
        get() = referenceZone.level

    public val mapsquareId: Int
        get() = ((zoneX ushr 3) shl 8) or (zoneZ ushr 3)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RebuildRegionZone

        if (referenceZone != other.referenceZone) return false

        return true
    }

    override fun hashCode(): Int {
        return referenceZone.hashCode()
    }

    override fun toString(): String =
        "RebuildRegionZone(" +
            "referenceZone=$referenceZone" +
            ")"
}
