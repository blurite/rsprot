package net.rsprot.protocol.game.outgoing.map

import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.game.outgoing.GameServerProtCategory
import net.rsprot.protocol.game.outgoing.map.util.RebuildRegionZone
import net.rsprot.protocol.message.OutgoingGameMessage

/**
 * Rebuild worldentity packet is used to build a new world entity block,
 * which will be rendered in the root world for the player.
 * @property index the index of the world entity (0-2048)
 * @property baseX the absolute base x coordinate of the world entity in the instance land
 * @property baseZ the absolute base z coordinate of the world entity in the instance land
 * @property zones the list of zones that will be built into the root world
 */
public class RebuildWorldEntity private constructor(
    private val _index: UShort,
    private val _baseX: UShort,
    private val _baseZ: UShort,
    public val zones: List<RebuildRegionZone?>,
) : OutgoingGameMessage {
    public constructor(
        index: Int,
        baseX: Int,
        baseZ: Int,
        sizeX: Int,
        sizeZ: Int,
        zoneProvider: RebuildWorldEntityZoneProvider,
    ) : this(
        index.toUShort(),
        baseX.toUShort(),
        baseZ.toUShort(),
        buildRebuildWorldEntityZones(index, sizeX, sizeZ, zoneProvider),
    ) {
        require(sizeX in 0..<13) {
            "Size x must be in range of 0..<13: $sizeX"
        }
        require(sizeZ in 0..<13) {
            "Size z must be in range of 0..<13: $sizeZ"
        }
        require(index in 0..<2048) {
            "Index must be in range of 0..<2048"
        }
        require(baseX in 0..<16384) {
            "Base x must be in range of 0..<16384"
        }
        require(baseZ in 0..<16384) {
            "Base z must be in range of 0..<16384"
        }
    }

    public val index: Int
        get() = _index.toInt()
    public val baseX: Int
        get() = _baseX.toInt()
    public val baseZ: Int
        get() = _baseZ.toInt()

    override val category: ServerProtCategory
        get() = GameServerProtCategory.HIGH_PRIORITY_PROT

    @Suppress("DuplicatedCode")
    override fun estimateSize(): Int {
        val header =
            Short.SIZE_BYTES +
                Short.SIZE_BYTES +
                Byte.SIZE_BYTES
        val notNullCount = zones.count { zone -> zone != null }
        val bitCount = (27 * notNullCount) + (zones.size - notNullCount)
        val bitBufByteCount = (bitCount + 7) ushr 3
        // While a little wasteful, it is expensive to determine the true
        // number of bytes necessary since we only transmit xteas for
        // each referenced mapsquare at most one time
        // In here, we just assume each zone belongs in a unique mapsquare
        // The buffers are pooled anyway so it isn't like we're typically
        // allocating a ton here, just picking a larger buffer out of the pool.
        val xteaSize = notNullCount * (4 * Int.SIZE_BYTES)
        return header +
            Short.SIZE_BYTES +
            bitBufByteCount +
            xteaSize
    }

    /**
     * Zone provider acts as a function to provide all the necessary information
     * needed for rebuild worldentity to function, in the order the client
     * expects it in.
     */
    public fun interface RebuildWorldEntityZoneProvider {
        /**
         * Provides a zone that the client must copy based on the parameters.
         * This 'provide' function will be called with the relative-to-worldentity zone coordinates,
         * so starting with 0,0 and ending before sizeX,sizeZ. The server is responsible for
         * looking up the actual zone that was copied for that world entity.
         * In order to calculate the mapsquare id for xtea keys, use [getMapsquareId].
         *
         * @param zoneX the x coordinate of the region zone
         * @param zoneZ the z coordinate of the region zone
         * @param level the level of the region zone
         * @return the zone to be copied, or null if there's no zone to be copied there.
         */
        public fun provide(
            index: Int,
            zoneX: Int,
            zoneZ: Int,
            level: Int,
        ): RebuildRegionZone?

        /**
         * Calculates the mapsquare id based on the **absolute** zone coordinates,
         * not the relative ones to the worldentity.
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
         * @param index the index of the world entity that is being built.
         * @param sizeX the width of the worldentity
         * @param sizeZ the length of the worldentity
         * @param zoneProvider the functional interface providing the necessary information
         * to be written to the client
         * @return a list of rebuild region zones (or nulls) for each zone in the build area.
         */
        private fun buildRebuildWorldEntityZones(
            index: Int,
            sizeX: Int,
            sizeZ: Int,
            zoneProvider: RebuildWorldEntityZoneProvider,
        ): List<RebuildRegionZone?> {
            val zones = ArrayList<RebuildRegionZone?>(4 * sizeX * sizeZ)
            for (level in 0..<4) {
                for (zoneX in 0..<sizeX) {
                    for (zoneZ in 0..<sizeZ) {
                        zones +=
                            zoneProvider.provide(
                                index,
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
