package net.rsprot.protocol.game.outgoing.map

import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.game.outgoing.GameServerProtCategory
import net.rsprot.protocol.game.outgoing.map.util.ReferenceZone
import net.rsprot.protocol.message.OutgoingGameMessage

/**
 * Rebuild worldentity packet is used to build a new world entity block,
 * which will be rendered in the root world for the player.
 * @property baseX the absolute base x coordinate of the world entity in the instance land
 * @property baseZ the absolute base z coordinate of the world entity in the instance land
 * @property zones the list of zones that will be built into the root world
 */
public class RebuildWorldEntityV4 private constructor(
    private val _baseX: UShort,
    private val _baseZ: UShort,
    public val zones: List<ReferenceZone?>,
) : OutgoingGameMessage {
    public constructor(
        baseX: Int,
        baseZ: Int,
        sizeX: Int,
        sizeZ: Int,
        zoneProvider: RebuildWorldEntityZoneProvider,
    ) : this(
        baseX.toUShort(),
        baseZ.toUShort(),
        buildRebuildWorldEntityZones(sizeX, sizeZ, zoneProvider),
    ) {
        require(sizeX in 0..<13) {
            "Size x must be in range of 0..<13: $sizeX"
        }
        require(sizeZ in 0..<13) {
            "Size z must be in range of 0..<13: $sizeZ"
        }
        require(baseX in 0..<16384) {
            "Base x must be in range of 0..<16384"
        }
        require(baseZ in 0..<16384) {
            "Base z must be in range of 0..<16384"
        }
    }

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
        return header +
            Short.SIZE_BYTES +
            bitBufByteCount
    }

    override fun toString(): String {
        return "RebuildWorldEntityV4(" +
            "zones=$zones, " +
            "baseX=$baseX, " +
            "baseZ=$baseZ" +
            ")"
    }

    /**
     * Zone provider acts as a function to provide all the necessary information
     * needed for rebuild worldentity to function, in the order the client
     * expects it in.
     */
    @JvmDefaultWithCompatibility
    public fun interface RebuildWorldEntityZoneProvider {
        /**
         * Provides a zone that the client must copy based on the parameters.
         * This 'provide' function will be called with the relative-to-worldentity zone coordinates,
         * so starting with 0,0 and ending before sizeX,sizeZ. The server is responsible for
         * looking up the actual zone that was copied for that world entity.
         *
         * @param zoneX the zone x coordinate of the region zone, relative to the south-westernmost zone
         * @param zoneZ the zone z coordinate of the region zone, relative to the south-westernmost zone
         * @param level the level of the region zone
         * @return the zone to be copied, or null if there's no zone to be copied there.
         */
        public fun provide(
            zoneX: Int,
            zoneZ: Int,
            level: Int,
        ): ReferenceZone?
    }

    private companion object {
        /**
         * Builds a list of rebuild region zones to be written to the client,
         * in order as the client expects them.
         * @param sizeX the width of the worldentity
         * @param sizeZ the length of the worldentity
         * @param zoneProvider the functional interface providing the necessary information
         * to be written to the client
         * @return a list of rebuild region zones (or nulls) for each zone in the build area.
         */
        private fun buildRebuildWorldEntityZones(
            sizeX: Int,
            sizeZ: Int,
            zoneProvider: RebuildWorldEntityZoneProvider,
        ): List<ReferenceZone?> {
            val zones = ArrayList<ReferenceZone?>(4 * sizeX * sizeZ)
            for (level in 0..<4) {
                for (zoneX in 0..<sizeX) {
                    for (zoneZ in 0..<sizeZ) {
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
