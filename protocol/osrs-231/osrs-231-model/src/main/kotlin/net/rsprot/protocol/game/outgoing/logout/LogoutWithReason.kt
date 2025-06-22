package net.rsprot.protocol.game.outgoing.logout

import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.game.outgoing.GameServerProtCategory
import net.rsprot.protocol.message.OutgoingGameMessage

/**
 * Logout with reason, much like [Logout], is used to
 * log the player out of the game. The only difference here
 * is that the user will be given a reason for why they were
 * logged out of the game, e.g. inactive for too long.
 *
 * Logout reasons table:
 * ```
 * | Id |   Type   |
 * |----|:--------:|
 * | 1  |  Kicked  |
 * | 2  | Updating |
 * ```
 *
 * @property reason the id of the reason to display (see table above)
 */
public class LogoutWithReason(
    public val reason: Int,
) : OutgoingGameMessage {
    override val category: ServerProtCategory
        get() = GameServerProtCategory.HIGH_PRIORITY_PROT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LogoutWithReason

        return reason == other.reason
    }

    override fun hashCode(): Int = reason

    override fun toString(): String = "LogoutWithReason(reason=$reason)"
}
