package net.rsprot.protocol.game.outgoing.map.util

import net.rsprot.crypto.xtea.XteaKey

/**
 * This class wraps a reference zone to be copied together with the respective
 * xtea key needed to decrypt the backing mapsquare.
 * @property referenceZone the zone to be copied from the static map
 * @property key the xtea key needed to decrypt the locs file in the cache of that respective mapsquare
 */
public class RebuildRegionZone private constructor(
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

    override fun toString(): String {
        return "RebuildRegionZone(" +
            "referenceZone=$referenceZone, " +
            "key=$key" +
            ")"
    }

    /**
     * A value class around zone objects that bitpacks all the properties into a single
     * integer to be written to the client as the client expects it.
     * @property rotation the rotation of the zone to be copied
     * @property zoneX the x coordinate of the static zone to be copied
     * @property zoneZ the z coordinate of the static zone to be copied
     * @property level the level of the static zone to be copied
     */
    @JvmInline
    public value class ReferenceZone private constructor(
        public val packed: Int,
    ) {
        public constructor(
            zoneX: Int,
            zoneZ: Int,
            level: Int,
            rotation: Int,
        ) : this(
            ((rotation and 0x3) shl 1)
                .or((zoneZ and 0x7FF) shl 3)
                .or((zoneX and 0x3FF) shl 14)
                .or((level and 0x3) shl 24),
        )

        public val rotation: Int
            get() = packed ushr 1 and 0x3
        public val zoneX: Int
            get() = packed ushr 14 and 0x3FF
        public val zoneZ: Int
            get() = packed ushr 3 and 0x7FF
        public val level: Int
            get() = packed ushr 24 and 0x3

        public val mapsquareId: Int
            get() = (zoneX shl 11) or (zoneZ shl 3)

        override fun toString(): String {
            return "ReferenceZone(" +
                "zoneX=$zoneX, " +
                "zoneZ=$zoneZ, " +
                "level=$level, " +
                "rotation=$rotation" +
                ")"
        }
    }
}
