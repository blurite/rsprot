package net.rsprot.protocol.game.outgoing.info

import net.rsprot.protocol.game.outgoing.info.npcinfo.NpcInfo
import net.rsprot.protocol.game.outgoing.info.playerinfo.PlayerInfo
import net.rsprot.protocol.game.outgoing.info.util.BuildArea
import net.rsprot.protocol.game.outgoing.info.worldentityinfo.WorldEntityInfo
import net.rsprot.protocol.internal.checkCommunicationThread
import net.rsprot.protocol.internal.game.outgoing.info.CoordGrid
import kotlin.math.max
import kotlin.math.min

public class Infos(
    public val playerInfo: PlayerInfo,
    public val npcInfo: NpcInfo,
    public val worldEntityInfo: WorldEntityInfo,
) {
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
    }

    /**
     * Updates the build area for this player. This should always perfectly correspond to
     * the actual build area that is sent via REBUILD_NORMAL or REBUILD_REGION packets.
     * This method takes the player's own absolute coordinates at the time of the map reload,
     * and picks the coordinate as 6 zones to the south-west of them.
     * This function will furthermore cap the coordinate to not go outside the usable map space.
     * @param playerAbsoluteX the absolute x coordinate of the player in the root world
     * @param playerAbsoluteZ the absolute z coordinate of the player in the root world
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
    }
}
