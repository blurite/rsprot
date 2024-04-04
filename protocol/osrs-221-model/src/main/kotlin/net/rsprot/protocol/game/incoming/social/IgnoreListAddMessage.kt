package net.rsprot.protocol.game.incoming.social

import net.rsprot.protocol.message.IncomingMessage

/**
 * Ignore list addition events are sent whenever the player
 * requests to add another player to their ignorelist
 * @property name the name of the player to add to their ignorelist
 */
public class IgnoreListAddMessage(
    public val name: String,
) : IncomingMessage {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IgnoreListAddMessage

        return name == other.name
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }

    override fun toString(): String {
        return "IgnoreListAddMessage(name='$name')"
    }
}
