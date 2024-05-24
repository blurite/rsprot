package net.rsprot.protocol.game.outgoing.misc.player

import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.game.outgoing.GameServerProtCategory
import net.rsprot.protocol.game.outgoing.zone.payload.util.CoordInBuildArea
import net.rsprot.protocol.message.OutgoingGameMessage

/**
 * Set map flag is used to set the red map flag on the minimap.
 * Use values 255, 255 to remove the map flag.
 * @property xInBuildArea the x coordinate within the build area
 * to render the map flag at.
 * @property zInBuildArea the z coordinate within the build area
 * to render the map flag at.
 */
public class SetMapFlag private constructor(
    private val coordInBuildArea: CoordInBuildArea,
) : OutgoingGameMessage {
    public constructor(
        xInBuildArea: Int,
        zInBuildArea: Int,
    ) : this(
        CoordInBuildArea(xInBuildArea, zInBuildArea),
    )

    public val xInBuildArea: Int
        get() = coordInBuildArea.xInBuildArea
    public val zInBuildArea: Int
        get() = coordInBuildArea.zInBuildArea
    override val category: ServerProtCategory
        get() = GameServerProtCategory.LOW_PRIORITY_PROT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SetMapFlag

        return coordInBuildArea == other.coordInBuildArea
    }

    override fun hashCode(): Int {
        return coordInBuildArea.hashCode()
    }

    override fun toString(): String {
        return "SetMapFlag(" +
            "xInBuildArea=$xInBuildArea, " +
            "zInBuildArea=$zInBuildArea" +
            ")"
    }
}
