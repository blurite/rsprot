package net.rsprot.protocol.game.outgoing.map.util

import net.rsprot.crypto.xtea.XteaKey

/**
 * This class wraps a reference zone to be copied together with the respective
 * xtea key needed to decrypt the backing mapsquare.
 * @property referenceZone the zone to be copied from the static map
 * @property key the xtea key needed to decrypt the locs file in the cache of that respective mapsquare
 */
public class RebuildRegionZone public constructor(
    public val referenceZone: ReferenceZone,
    public val key: XteaKey,
) {
    public constructor(
        zoneX: Int,
        zoneZ: Int,
        level: Int,
        rotation: Int,
        key: XteaKey,
    ) : this(
        ReferenceZone(
            zoneX,
            zoneZ,
            level,
            rotation,
        ),
        key,
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
        if (key != other.key) return false

        return true
    }

    override fun hashCode(): Int {
        var result = referenceZone.hashCode()
        result = 31 * result + key.hashCode()
        return result
    }

    override fun toString(): String =
        "RebuildRegionZone(" +
            "referenceZone=$referenceZone, " +
            "key=$key" +
            ")"
}
