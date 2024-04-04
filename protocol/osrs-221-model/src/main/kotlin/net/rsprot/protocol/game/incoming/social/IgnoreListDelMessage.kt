package net.rsprot.protocol.game.incoming.social

import net.rsprot.protocol.message.IncomingMessage

/**
 * Ignore list deletion messages are sent whenever the player
 * requests to delete another player from their ignorelist
 * @property name the name of the player to delete from their ignorelist
 */
public class IgnoreListDelMessage(
    public val name: String,
) : IncomingMessage {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IgnoreListDelMessage

        return name == other.name
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }

    override fun toString(): String {
        return "IgnoreListDelMessage(name='$name')"
    }
}
