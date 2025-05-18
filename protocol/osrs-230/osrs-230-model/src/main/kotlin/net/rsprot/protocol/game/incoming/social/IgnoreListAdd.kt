package net.rsprot.protocol.game.incoming.social

import net.rsprot.protocol.ClientProtCategory
import net.rsprot.protocol.game.incoming.GameClientProtCategory
import net.rsprot.protocol.message.IncomingGameMessage

/**
 * Ignore list addition events are sent whenever the player
 * requests to add another player to their ignorelist
 * @property name the name of the player to add to their ignorelist
 */
public class IgnoreListAdd(
    public val name: String,
) : IncomingGameMessage {
    override val category: ClientProtCategory
        get() = GameClientProtCategory.USER_EVENT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IgnoreListAdd

        return name == other.name
    }

    override fun hashCode(): Int = name.hashCode()

    override fun toString(): String = "IgnoreListAdd(name='$name')"
}
