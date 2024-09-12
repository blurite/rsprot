package net.rsprot.protocol.game.outgoing.info.playerinfo

import net.rsprot.protocol.common.game.outgoing.info.CoordGrid
import net.rsprot.protocol.game.outgoing.info.util.BuildArea

/**
 * A class which wraps the details of a player info implementation in a specific world.
 * @property worldId the id of the world this info object is tracking.
 */
internal class PlayerInfoWorldDetails(
    internal var worldId: Int,
) {
    /**
     * The coordinate from which distance checks are done against other players.
     */
    internal var renderCoord: CoordGrid = CoordGrid.INVALID

    /**
     * The entire build area of this world - this effectively caps what we can see
     * to be within this block of land. Anything outside will be excluded.
     */
    internal var buildArea: BuildArea = BuildArea.INVALID

    internal fun onAlloc(worldId: Int) {
        this.worldId = worldId
        this.renderCoord = CoordGrid.INVALID
        this.buildArea = BuildArea.INVALID
    }
}
