package net.rsprot.protocol.game.outgoing.map

import io.netty.buffer.ByteBuf
import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.game.outgoing.GameServerProtCategory
import net.rsprot.protocol.game.outgoing.info.playerinfo.PlayerInfo
import net.rsprot.protocol.game.outgoing.map.util.RebuildRegionZone
import net.rsprot.protocol.message.OutgoingGameMessage

/**
 * Rebuild worldentity packet is used to build a new world entity block,
 * which will be rendered in the root world for the player.
 * @property index the index of the world entity (0-2048)
 * @property baseX the absolute base x coordinate of the world entity in the instance land
 * @property baseZ the absolute base z coordinate of the world entity in the instance land
 * @property zones the list of zones that will be built into the root world
 * @property gpiInitBlock the player info initialization block for the world entity
 */
public class RebuildWorldEntity private constructor(
    private val _index: UShort,
    private val _baseX: UShort,
    private val _baseZ: UShort,
    public val zones: List<RebuildRegionZone?>,
    public val gpiInitBlock: ByteBuf,
) : OutgoingGameMessage {
    public constructor(
        index: Int,
        baseX: Int,
        baseZ: Int,
        sizeX: Int,
        sizeZ: Int,
        zoneProvider: RebuildWorldEntityZoneProvider,
        playerInfo: PlayerInfo,
    ) : this(
        index.toUShort(),
        baseX.toUShort(),
        baseZ.toUShort(),
        buildRebuildWorldEntityZones(index, sizeX, sizeZ, zoneProvider),
        initializePlayerInfo(playerInfo, index),
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
         * @param zoneX the x coordinate of the static zone to be copied
         * @param zoneZ the z coordinate of the static zone to be copied
         * @param level the level of the static zone to be copied
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
        ): Int {
            return (zoneX shl 11) or (zoneZ shl 3)
        }
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

        private const val PLAYER_INFO_BLOCK_SIZE = ((30 + (2046 * 18)) + Byte.SIZE_BITS - 1) ushr 3

        /**
         * Initializes the player info block into a buffer provided by allocator in the playerinfo object
         * @param playerInfo the player info protocol of this player to be initialized
         * @return a buffer containing the initialization block of the player info protocol
         */
        private fun initializePlayerInfo(
            playerInfo: PlayerInfo,
            worldId: Int,
        ): ByteBuf {
            val allocator = playerInfo.allocator
            val buffer = allocator.buffer(PLAYER_INFO_BLOCK_SIZE)
            playerInfo.handleAbsolutePlayerPositions(worldId, buffer)
            return buffer
        }
    }
}
