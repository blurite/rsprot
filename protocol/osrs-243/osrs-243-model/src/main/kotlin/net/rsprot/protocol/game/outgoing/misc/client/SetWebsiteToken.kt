package net.rsprot.protocol.game.outgoing.misc.client

import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.game.outgoing.GameServerProtCategory
import net.rsprot.protocol.message.OutgoingGameMessage
import net.rsprot.protocol.message.util.estimateTextSize

/**
 * Sets the website session token used when changing the player's display name.
 * @property token the session token
 */
public class SetWebsiteToken(
    public val token: String,
) : OutgoingGameMessage {
    override val category: ServerProtCategory
        get() = GameServerProtCategory.HIGH_PRIORITY_PROT

    override fun estimateSize(): Int {
        return estimateTextSize(token)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SetWebsiteToken

        return token == other.token
    }

    override fun hashCode(): Int = token.hashCode()

    override fun toString(): String = "SetWebsiteToken(token='$token')"
}
