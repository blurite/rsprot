package net.rsprot.protocol.game.outgoing.misc.client

import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.game.outgoing.GameServerProtCategory
import net.rsprot.protocol.message.OutgoingGameMessage

/**
 * Minimap toggle is used to modify the state of the minimap
 * and the attached compass.
 *
 * Minimap states table:
 * ```
 * | Id |           Description           |
 * |----|:-------------------------------:|
 * | 0  |             Enabled             |
 * | 1  |       Minimap unclickable       |
 * | 2  |          Minimap hidden         |
 * | 3  |          Compass hidden         |
 * | 4  | Map unclickable, compass hidden |
 * | 5  |             Disabled            |
 * ```
 *
 * @property minimapState the minimap state to set (see table above)
 */
public class MinimapToggle(
    public val minimapState: Int,
) : OutgoingGameMessage {
    override val category: ServerProtCategory
        get() = GameServerProtCategory.LOW_PRIORITY_PROT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MinimapToggle

        return minimapState == other.minimapState
    }

    override fun hashCode(): Int = minimapState

    override fun toString(): String = "MinimapToggle(minimapState=$minimapState)"
}
