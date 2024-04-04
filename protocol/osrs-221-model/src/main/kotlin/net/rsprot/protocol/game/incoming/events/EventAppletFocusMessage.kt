package net.rsprot.protocol.game.incoming.events

import net.rsprot.protocol.message.IncomingMessage

/**
 * Applet focus events are sent whenever the client either loses or gains focus.
 * This can be seen by minimizing and maximizing the clients.
 * @property inFocus whether the client was put into focus or out of focus
 */
public class EventAppletFocusMessage(
    public val inFocus: Boolean,
) : IncomingMessage {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EventAppletFocusMessage

        return inFocus == other.inFocus
    }

    override fun hashCode(): Int {
        return inFocus.hashCode()
    }

    override fun toString(): String {
        return "EventAppletFocusMessage(inFocus=$inFocus)"
    }
}
