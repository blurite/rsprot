package net.rsprot.protocol.game.incoming.resumed

import net.rsprot.protocol.ClientProtCategory
import net.rsprot.protocol.game.incoming.GameClientProtCategory
import net.rsprot.protocol.message.IncomingGameMessage

/**
 * Name dialogs are sent whenever a player enters the name of a player
 * into the chatbox input box, e.g. to enter someone else's player-owned
 * house.
 * @property name the name of the player entered
 */
public class ResumePNameDialog(
    public val name: String,
) : IncomingGameMessage {
    override val category: ClientProtCategory
        get() = GameClientProtCategory.USER_EVENT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ResumePNameDialog

        return name == other.name
    }

    override fun hashCode(): Int = name.hashCode()

    override fun toString(): String = "ResumePNameDialog(name='$name')"
}
