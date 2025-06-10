package net.rsprot.protocol.game.outgoing.misc.client

import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.game.outgoing.GameServerProtCategory
import net.rsprot.protocol.message.OutgoingGameMessage

/**
 * Update reboot timer is used to start the shut-down timer
 * in preparation of an update.
 * @property gameCycles the number of game cycles (600ms/gc)
 * until the shut-down is complete.
 * If the number is set to zero, any existing reboot timers
 * will be cleared out.
 * The maximum possible value is 65535, which is equal to just
 * below 11 hours.
 */
public class UpdateRebootTimer(
    public val gameCycles: Int,
) : OutgoingGameMessage {
    override val category: ServerProtCategory
        get() = GameServerProtCategory.LOW_PRIORITY_PROT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UpdateRebootTimer

        return gameCycles == other.gameCycles
    }

    override fun hashCode(): Int = gameCycles

    override fun toString(): String =
        "UpdateRebootTimer(" +
            "gameCycles=$gameCycles" +
            ")"
}
