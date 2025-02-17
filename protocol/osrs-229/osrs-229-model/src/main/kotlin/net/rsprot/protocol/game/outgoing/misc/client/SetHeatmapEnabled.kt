package net.rsprot.protocol.game.outgoing.misc.client

import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.game.outgoing.GameServerProtCategory
import net.rsprot.protocol.message.OutgoingGameMessage

/**
 * Set heatmap enabled packet is used to either enable or
 * disabled the heatmap, which is rendered over the
 * world map in OldSchool.
 * This packet utilizes high resolution coordinate info
 * about all the players of the game through player info
 * packet, so in order for it to properly function,
 * high resolution information must be sent for everyone
 * in the game.
 */
public class SetHeatmapEnabled(
    public val enabled: Boolean,
) : OutgoingGameMessage {
    override val category: ServerProtCategory
        get() = GameServerProtCategory.LOW_PRIORITY_PROT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SetHeatmapEnabled

        return enabled == other.enabled
    }

    override fun hashCode(): Int = enabled.hashCode()

    override fun toString(): String = "SetHeatmapEnabled(enabled=$enabled)"
}
