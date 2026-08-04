package net.rsprot.protocol.game.outgoing.misc.client

import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.game.outgoing.GameServerProtCategory
import net.rsprot.protocol.message.OutgoingGameMessage

/**
 * Resets the interaction mode for a specific world.
 * @property worldId the id of the world to modify.
 */
public class ResetInteractionMode private constructor(
    private val _worldId: Short,
) : OutgoingGameMessage {
    public constructor(worldId: Int) : this(worldId.toShort())

    public val worldId: Int
        get() = _worldId.toInt()
    override val category: ServerProtCategory
        get() = GameServerProtCategory.HIGH_PRIORITY_PROT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ResetInteractionMode) return false

        if (_worldId != other._worldId) return false

        return true
    }

    override fun hashCode(): Int {
        return _worldId.toInt()
    }

    override fun toString(): String {
        return "SetInteractionMode(" +
            "worldId=$worldId" +
            ")"
    }
}
