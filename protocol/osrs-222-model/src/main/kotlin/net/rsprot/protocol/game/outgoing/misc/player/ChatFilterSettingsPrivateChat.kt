package net.rsprot.protocol.game.outgoing.misc.player

import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.game.outgoing.GameServerProtCategory
import net.rsprot.protocol.message.OutgoingGameMessage

/**
 * Chat filter settings packed is used to set the private
 * chat filter.
 *
 * Chat filters table:
 * ```
 * | Id |   Type   |
 * |----|:--------:|
 * | 0  |    On    |
 * | 1  |  Friends |
 * | 2  |    Off   |
 * ```
 *
 * @property privateChatFilter the private chat filter value.
 */
public class ChatFilterSettingsPrivateChat(
    public val privateChatFilter: Int,
) : OutgoingGameMessage {
    override val category: ServerProtCategory
        get() = GameServerProtCategory.HIGH_PRIORITY_PROT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ChatFilterSettingsPrivateChat

        return privateChatFilter == other.privateChatFilter
    }

    override fun hashCode(): Int {
        return privateChatFilter
    }

    override fun toString(): String {
        return "ChatFilterSettingsPrivateChat(privateChatFilter=$privateChatFilter)"
    }
}
