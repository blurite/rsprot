package net.rsprot.protocol.game.incoming.resumed

import net.rsprot.protocol.message.IncomingMessage

/**
 * Resume p count dialogue is sent whenever a player enters an
 * integer to the input box, e.g. to withdraw an item in x-quantity.
 * @property count the count entered. While this can only be a positive
 * integer for manually-entered inputs, it is **not** guaranteed to always
 * be positive. Clientscripts can invoke this event with negative values to
 * represent various potential response codes.
 */
public class ResumePCountDialogMessage(
    public val count: Int,
) : IncomingMessage {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ResumePCountDialogMessage

        return count == other.count
    }

    override fun hashCode(): Int {
        return count
    }

    override fun toString(): String {
        return "ResumePCountDialogMessage(count=$count)"
    }
}
