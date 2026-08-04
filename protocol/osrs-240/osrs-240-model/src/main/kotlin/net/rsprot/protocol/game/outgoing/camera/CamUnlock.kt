package net.rsprot.protocol.game.outgoing.camera

import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.game.outgoing.GameServerProtCategory
import net.rsprot.protocol.message.OutgoingGameMessage

/**
 * Unlocks the camera's min and max pitch angle, allowing one to pan the camera fully vertical,
 * or into the ground.
 */
public class CamUnlock(
    public val unlock: Boolean,
) : OutgoingGameMessage {
    override val category: ServerProtCategory
        get() = GameServerProtCategory.LOW_PRIORITY_PROT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CamUnlock

        return unlock == other.unlock
    }

    override fun hashCode(): Int {
        return unlock.hashCode()
    }

    override fun toString(): String =
        "CamUnlock(" +
            "unlock=$unlock" +
            ")"
}
