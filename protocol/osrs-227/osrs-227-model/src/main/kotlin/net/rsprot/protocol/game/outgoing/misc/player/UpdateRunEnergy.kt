package net.rsprot.protocol.game.outgoing.misc.player

import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.game.outgoing.GameServerProtCategory
import net.rsprot.protocol.message.OutgoingGameMessage

/**
 * Update runenergy packet is used to modify the player's current
 * run energy. 100 units equals one percentage on the run orb,
 * meaning a value of 10,000 is equal to 100% run energy.
 */
public class UpdateRunEnergy(
    public val runenergy: Int,
) : OutgoingGameMessage {
    override val category: ServerProtCategory
        get() = GameServerProtCategory.LOW_PRIORITY_PROT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UpdateRunEnergy

        return runenergy == other.runenergy
    }

    override fun hashCode(): Int = runenergy

    override fun toString(): String = "UpdateRunEnergy(runenergy=$runenergy)"
}
