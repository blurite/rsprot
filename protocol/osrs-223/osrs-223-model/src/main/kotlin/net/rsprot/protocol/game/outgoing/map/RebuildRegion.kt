package net.rsprot.protocol.game.outgoing.map

import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.game.outgoing.GameServerProtCategory
import net.rsprot.protocol.game.outgoing.map.util.RebuildRegionZone
import net.rsprot.protocol.message.OutgoingGameMessage

/**
 * Rebuild region is used to send a dynamic map to the client,
 * built up out of zones (8x8x1 tiles), allowing for any kind
 * of unique instancing to occur.
 * @property zoneX the x coordinate of the center zone around
 * which the build area is built
 * @property zoneZ the z coordinate of the center zone around
 * which the build area is built
 * @property reload whether to forcibly reload the map client-sided.
 * If this property is false, the client will only reload if
 * the last rebuild had difference [zoneX] or [zoneZ] coordinates
 * than this one.
 * @property zones the list of zones to build, in a specific order.
 */
public class RebuildRegion private constructor(
    private val _zoneX: UShort,
    private val _zoneZ: UShort,
    public val reload: Boolean,
    public val zones: List<RebuildRegionZone?>,
) : OutgoingGameMessage {
    public constructor(
        zoneX: Int,
        zoneZ: Int,
        reload: Boolean,
        zoneProvider: RebuildRegionZoneProvider,
    ) : this(
        zoneX.toUShort(),
        zoneZ.toUShort(),
        reload,
        buildRebuildRegionZones(
            zoneX,
            zoneZ,
            zoneProvider,
        ),
    )

    public val zoneX: Int
        get() = _zoneX.toInt()
    public val zoneZ: Int
        get() = _zoneZ.toInt()
    override val category: ServerProtCategory
        get() = GameServerProtCategory.HIGH_PRIORITY_PROT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RebuildRegion

        if (_zoneX != other._zoneX) return false
        if (_zoneZ != other._zoneZ) return false
        if (reload != other.reload) return false
        if (zones != other.zones) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _zoneX.hashCode()
        result = 31 * result + _zoneZ.hashCode()
        result = 31 * result + reload.hashCode()
        result = 31 * result + zones.hashCode()
        return result
    }

    override fun toString(): String =
        "RebuildRegion(" +
            "zoneX=$zoneX, " +
            "zoneZ=$zoneZ, " +
            "reload=$reload, " +
            "zones=$zones" +
            ")"

    /**
     * Zone provider acts as a function to provide all the necessary information
     * needed for rebuild region to function, in the order the client
     * expects it in.
     */
    public fun interface RebuildRegionZoneProvider {
        /**
         * Provides a zone that the client must copy based on the parameters.
         * In order to calculate the mapsquare id for xtea keys, use [getMapsquareId].
         *
         * @param zoneX the x coordinate of the static zone to be copied
         * @param zoneZ the z coordinate of the static zone to be copied
         * @param level the level of the static zone to be copied
         * @return the zone to be copied, or null if there's no zone to be copied there.
         */
        public fun provide(
            zoneX: Int,
            zoneZ: Int,
            level: Int,
        ): RebuildRegionZone?

        /**
         * Calculates the mapsquare id based on the zone coordinates.
         * @param zoneX the x coordinate of the zone
         * @param zoneZ the z coordinate of the zone
         */
        public fun getMapsquareId(
            zoneX: Int,
            zoneZ: Int,
        ): Int = (zoneX and 0x7FF ushr 3 shl 8) or (zoneZ and 0x7FF ushr 3)
    }

    private companion object {
        /**
         * Builds a list of rebuild region zones to be written to the client,
         * in order as the client expects them.
         * @param centerZoneX the center zone x coordinate around which the build area is built
         * @param centerZoneZ the center zone z coordinate around which the build area is built
         * @param zoneProvider the functional interface providing the necessary information
         * to be written to the client
         * @return a list of rebuild region zones (or nulls) for each zone in the build area.
         */
        private fun buildRebuildRegionZones(
            centerZoneX: Int,
            centerZoneZ: Int,
            zoneProvider: RebuildRegionZoneProvider,
        ): List<RebuildRegionZone?> {
            val zones = ArrayList<RebuildRegionZone?>(4 * 13 * 13)
            for (level in 0..<4) {
                for (zoneX in (centerZoneX - 6)..(centerZoneX + 6)) {
                    for (zoneZ in (centerZoneZ - 6)..(centerZoneZ + 6)) {
                        zones +=
                            zoneProvider.provide(
                                zoneX,
                                zoneZ,
                                level,
                            )
                    }
                }
            }
            return zones
        }
    }
}
