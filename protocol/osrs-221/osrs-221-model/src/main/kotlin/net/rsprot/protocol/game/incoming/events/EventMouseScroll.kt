package net.rsprot.protocol.game.incoming.events

import net.rsprot.protocol.ClientProtCategory
import net.rsprot.protocol.game.incoming.GameClientProtCategory
import net.rsprot.protocol.message.IncomingGameMessage

/**
 * Mouse scroll message is sent whenever the user scrolls using their mouse.
 * @property mouseWheelRotation the number of "clicks" the mouse wheel has rotated.
 * If the mouse wheel was rotated up/away from the user, negative value is sent,
 * and if the wheel was rotated down/towards the user, a positive value is sent.
 */
public class EventMouseScroll(
    public val mouseWheelRotation: Int,
) : IncomingGameMessage {
    override val category: ClientProtCategory
        get() = GameClientProtCategory.CLIENT_EVENT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EventMouseScroll

        return mouseWheelRotation == other.mouseWheelRotation
    }

    override fun hashCode(): Int = mouseWheelRotation

    override fun toString(): String = "EventMouseScroll(mouseWheelRotation=$mouseWheelRotation)"
}
