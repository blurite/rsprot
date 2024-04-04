package net.rsprot.protocol.game.incoming.social

import net.rsprot.protocol.message.IncomingMessage

/**
 * Friend list deletion messages are sent whenever the player
 * requests to delete another user from their friend list.
 * @property name the name of the player to delete
 */
public class FriendListDel(
    public val name: String,
) : IncomingMessage {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FriendListDel

        return name == other.name
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }

    override fun toString(): String {
        return "FriendListDel(name='$name')"
    }
}
