package net.rsprot.protocol.game.incoming.locs

import net.rsprot.protocol.message.IncomingMessage

/**
 * OpLoc6 message is fired whenever a player clicks examine on a loc.
 * @property id the id of the loc (if multiloc, transformed to the
 * currently visible variant)
 */
@Suppress("DuplicatedCode", "MemberVisibilityCanBePrivate")
public class OpLoc6Message(
    public val id: Int,
) : IncomingMessage {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as OpLoc6Message

        return id == other.id
    }

    override fun hashCode(): Int {
        return id
    }

    override fun toString(): String {
        return "OpLoc6Message(id=$id)"
    }
}
