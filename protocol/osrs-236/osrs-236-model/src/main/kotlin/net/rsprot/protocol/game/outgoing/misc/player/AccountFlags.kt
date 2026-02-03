package net.rsprot.protocol.game.outgoing.misc.player

import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.game.outgoing.GameServerProtCategory
import net.rsprot.protocol.message.OutgoingGameMessage

/**
 * Account flags are used to set certain features in the client for given players.
 *
 * Below is a table of known flags:
 *
 * ```
 * | Bit | Feature                                |
 * |-----|----------------------------------------|
 * | 35  | Enable Lua Plugin Development Commands |
 * ```
 */
public class AccountFlags(
    public val flags: Long,
) : OutgoingGameMessage {
    override val category: ServerProtCategory
        get() = GameServerProtCategory.LOW_PRIORITY_PROT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AccountFlags

        return flags == other.flags
    }

    override fun hashCode(): Int {
        return flags.hashCode()
    }

    override fun toString(): String = "AccountFlags(flags=$flags)"
}
