package net.rsprot.protocol.game.incoming.social

import net.rsprot.protocol.message.IncomingGameMessage

/**
 * Friend list add messages are sent when the player requests
 * to add another player to their friend list
 * @property name the name of the player to add to the friend list
 */
public class FriendListAdd(
    public val name: String,
) : IncomingGameMessage {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FriendListAdd

        return name == other.name
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }

    override fun toString(): String {
        return "FriendListAdd(name='$name')"
    }
}
