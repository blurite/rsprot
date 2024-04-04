package net.rsprot.protocol.game.incoming.resumed

import net.rsprot.protocol.message.IncomingMessage

/**
 * Name dialogs are sent whenever a player enters the name of a player
 * into the chatbox input box, e.g. to enter someone else's player-owned
 * house.
 * @property name the name of the player entered
 */
public class ResumePNameDialogMessage(
    public val name: String,
) : IncomingMessage {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ResumePNameDialogMessage

        return name == other.name
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }

    override fun toString(): String {
        return "ResumePNameDialogMessage(name='$name')"
    }
}
