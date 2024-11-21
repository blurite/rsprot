package net.rsprot.protocol.game.outgoing.misc.player

import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.game.outgoing.GameServerProtCategory
import net.rsprot.protocol.message.OutgoingGameMessage

/**
 * Update runweight packet is used to modify the player's current
 * equipment and inventory weight, in grams.
 */
public class UpdateRunWeight(
    public val runweight: Int,
) : OutgoingGameMessage {
    override val category: ServerProtCategory
        get() = GameServerProtCategory.LOW_PRIORITY_PROT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UpdateRunWeight

        return runweight == other.runweight
    }

    override fun hashCode(): Int = runweight

    override fun toString(): String = "UpdateRunWeight(runweight=$runweight)"
}
