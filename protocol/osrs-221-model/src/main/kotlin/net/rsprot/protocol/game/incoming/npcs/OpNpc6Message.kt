package net.rsprot.protocol.game.incoming.npcs

import net.rsprot.protocol.message.IncomingMessage

/**
 * OpNpc6 message is fired when a player clicks the 'Examine' option on a npc.
 * @property id the config id of the npc clicked
 */
@Suppress("MemberVisibilityCanBePrivate")
public class OpNpc6Message(
    public val id: Int,
) : IncomingMessage {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as OpNpc6Message

        return id == other.id
    }

    override fun hashCode(): Int {
        return id
    }

    override fun toString(): String {
        return "OpNpc6Message(id=$id)"
    }
}
