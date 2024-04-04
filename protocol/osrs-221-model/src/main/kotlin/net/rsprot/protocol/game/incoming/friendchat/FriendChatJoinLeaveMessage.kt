package net.rsprot.protocol.game.incoming.friendchat

import net.rsprot.protocol.message.IncomingMessage

/**
 * Friend chat join-leave message is sent when the player joins or leaves
 * a friend chat channel.
 * @property name the name of the player whose friend chat channel to join,
 * or null if the player is leaving a friend chat channel
 */
public class FriendChatJoinLeaveMessage(
    public val name: String?,
) : IncomingMessage {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FriendChatJoinLeaveMessage

        return name == other.name
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }

    override fun toString(): String {
        return "FriendChatJoinLeaveMessage(name='$name')"
    }
}
