package net.rsprot.protocol.game.incoming.friendchat

import net.rsprot.protocol.ClientProtCategory
import net.rsprot.protocol.game.incoming.GameClientProtCategory
import net.rsprot.protocol.message.IncomingGameMessage

/**
 * Friend chat kick is sent when the owner requests to click another
 * player from their friend chat.
 * @property name the name of the player to kick
 */
public class FriendChatKick(
    public val name: String,
) : IncomingGameMessage {
    override val category: ClientProtCategory
        get() = GameClientProtCategory.USER_EVENT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FriendChatKick

        return name == other.name
    }

    override fun hashCode(): Int = name.hashCode()

    override fun toString(): String = "FriendChatKick(name='$name')"
}
