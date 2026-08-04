package net.rsprot.protocol.game.outgoing.info

import net.rsprot.protocol.game.outgoing.info.npcinfo.NpcInfo
import net.rsprot.protocol.game.outgoing.info.npcinfo.SetNpcUpdateOrigin
import net.rsprot.protocol.game.outgoing.info.playerinfo.PlayerInfo
import net.rsprot.protocol.game.outgoing.info.util.BuildArea
import net.rsprot.protocol.game.outgoing.info.worldentityinfo.WorldEntityInfo
import net.rsprot.protocol.game.outgoing.worldentity.SetActiveWorldV2
import net.rsprot.protocol.internal.checkCommunicationThread
import net.rsprot.protocol.internal.game.outgoing.info.CoordGrid
import kotlin.math.max
import kotlin.math.min

public class Infos(
    public val playerInfo: PlayerInfo,
    public val npcInfo: NpcInfo,
    public val worldEntityInfo: WorldEntityInfo,
) {
    private var coord: CoordGrid = CoordGrid.INVALID
    private var buildArea: BuildArea = BuildArea.INVALID

    /**
     * Updates the current real absolute coordinate of the local player in the world.
     * @param level the current height level of the player
     * @param x the current absolute x coordinate of the player
     * @param z the current absolute z coordinate of the player
     */
    public fun updateRootCoord(
        level: Int,
        x: Int,
        z: Int,
    ) {
        checkCommunicationThread()
        val coordGrid = CoordGrid(level, x, z)
        worldEntityInfo.updateRootCoord(coordGrid)
        npcInfo.updateRootCoord(coordGrid)
        playerInfo.updateRootCoord(coordGrid)
        this.coord = coordGrid
    }

    /**
     * Updates the build area for this player. This should always perfectly correspond to
     * the actual build area that is sent via REBUILD_NORMAL or REBUILD_REGION packets.
     * This method takes the player's own absolute coordinates at the time of the map reload,
     * and picks the coordinate as 6 zones to the south-west of them. Note that if the player
     * is on a world entity at the time, it should correspond to the world entity's coordgrid
     * in the root world.
     * This function will furthermore cap the coordinate to not go outside the usable map space.
     * @param playerAbsoluteX the absolute x coordinate in the root world
     * @param playerAbsoluteZ the absolute z coordinate in the root world
     */
    public fun updateRootBuildAreaCenteredOnPlayer(
        playerAbsoluteX: Int,
        playerAbsoluteZ: Int,
    ) {
        val centerZoneX = playerAbsoluteX ushr 3
        val centerZoneZ = playerAbsoluteZ ushr 3
        val swZoneX = max(0, centerZoneX - 6)
        val swZoneZ = max(0, centerZoneZ - 6)
        val neZoneX = min(0x7FF, centerZoneX + 6)
        val neZoneZ = min(0x7FF, centerZoneZ + 6)
        val widthInZones = (neZoneX - swZoneX) + 1
        val heightInZones = (neZoneZ - swZoneZ) + 1
        updateRootBuildArea(swZoneX, swZoneZ, widthInZones, heightInZones)
    }

    /**
     * Updates the build area for this player. This should always perfectly correspond to
     * the actual build area that is sent via REBUILD_NORMAL or REBUILD_REGION packets.
     * @property zoneX the south-western zone x coordinate of the build area
     * @property zoneZ the south-western zone z coordinate of the build area
     * @property widthInZones the build area width in zones (typically 13, meaning 104 tiles)
     * @property heightInZones the build area height in zones (typically 13, meaning 104 tiles)
     */
    @JvmOverloads
    public fun updateRootBuildArea(
        zoneX: Int,
        zoneZ: Int,
        widthInZones: Int = BuildArea.DEFAULT_BUILD_AREA_SIZE,
        heightInZones: Int = BuildArea.DEFAULT_BUILD_AREA_SIZE,
    ) {
        updateRootBuildArea(BuildArea(zoneX, zoneZ, widthInZones, heightInZones))
    }

    /**
     * Updates the build area for this player. This should always perfectly correspond to
     * the actual build area that is sent via REBUILD_NORMAL or REBUILD_REGION packets.
     * @param buildArea the build area in which everything is rendered.
     */
    public fun updateRootBuildArea(buildArea: BuildArea) {
        checkCommunicationThread()
        worldEntityInfo.updateRootBuildArea(buildArea)
        this.buildArea = buildArea
    }

    /**
     * Builds a data class containing all the info packets and necessary metadata in a neat package.
     * Servers can simply iterate through the data here and send the packets as they are provided.
     * World ids and active levels are provided alongside for easy zone synchronization.
     */
    public fun getPackets(): InfoPackets {
        val worldEntityInfoResult = worldEntityInfo.toPacketResult()
        val playerInfoResult = playerInfo.toPacketResult()
        val rootNpcInfoResult = npcInfo.toPacketResult(NpcInfo.ROOT_WORLD)

        val addedWorldIndices = worldEntityInfo.getAddedWorldEntityIndices()
        val removedWorldIndices = worldEntityInfo.getRemovedWorldEntityIndices()
        val allWorldIndices = worldEntityInfo.getAllWorldEntityIndices()

        val coord = this.coord
        val buildArea = this.buildArea
        val rootWorldCoord = worldEntityInfo.getCoordGridInRootWorld(coord)
        val currentWorldEntityIndex = worldEntityInfo.getWorldEntity(coord)

        val activeRootLevel = rootWorldCoord.level
        val rootWorldInfoPackets =
            RootWorldInfoPackets(
                activeLevel = activeRootLevel,
                activeWorld = SetActiveWorldV2.getRoot(activeRootLevel),
                npcUpdateOrigin =
                    SetNpcUpdateOrigin(
                        rootWorldCoord.x - (buildArea.zoneX shl 3),
                        rootWorldCoord.z - (buildArea.zoneZ shl 3),
                    ),
                worldEntityInfo = worldEntityInfoResult,
                playerInfo = playerInfoResult,
                npcInfo = rootNpcInfoResult,
            )

        val activeWorlds = ArrayList<WorldInfoPackets>(allWorldIndices.size)
        for (worldId in allWorldIndices) {
            val npcInfoResult = npcInfo.toPacketResult(worldId)
            val activeLevel =
                if (currentWorldEntityIndex == worldId) {
                    coord.level
                } else {
                    val level = worldEntityInfo.getActiveLevel(worldId)
                    // Should never happen, but just in case fall back to player's own level if it does
                    if (level == -1) coord.level else level
                }
            val added = worldId in addedWorldIndices
            activeWorlds.add(
                WorldInfoPackets(
                    worldId = worldId,
                    activeLevel = activeLevel,
                    added = added,
                    activeWorld =
                        SetActiveWorldV2(
                            SetActiveWorldV2.DynamicWorldType(
                                worldId,
                                activeLevel,
                            ),
                        ),
                    npcUpdateOrigin = SetNpcUpdateOrigin.DYNAMIC,
                    npcInfo = npcInfoResult,
                ),
            )
        }

        return InfoPackets(
            rootWorldInfoPackets = rootWorldInfoPackets,
            removedWorldIndices = removedWorldIndices,
            activeWorlds = activeWorlds,
        )
    }
}
