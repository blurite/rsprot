package net.rsprot.protocol.game.incoming.events

import net.rsprot.protocol.message.IncomingMessage

/**
 * Mouse scroll message is sent whenever the user scrolls using their mouse.
 * @property mouseWheelRotation the number of "clicks" the mouse wheel has rotated.
 * If the mouse wheel was rotated up/away from the user, negative value is sent,
 * and if the wheel was rotated down/towards the user, a positive value is sent.
 */
public class EventMouseScrollMessage(
    public val mouseWheelRotation: Int,
) : IncomingMessage {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EventMouseScrollMessage

        return mouseWheelRotation == other.mouseWheelRotation
    }

    override fun hashCode(): Int {
        return mouseWheelRotation
    }

    override fun toString(): String {
        return "EventMouseScrollMessage(mouseWheelRotation=$mouseWheelRotation)"
    }
}
