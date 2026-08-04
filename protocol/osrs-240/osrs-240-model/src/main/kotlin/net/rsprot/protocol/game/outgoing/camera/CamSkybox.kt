package net.rsprot.protocol.game.outgoing.camera

import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.game.outgoing.GameServerProtCategory
import net.rsprot.protocol.message.OutgoingGameMessage

/**
 * Sets the skybox model to render.
 */
public class CamSkybox(
    public val model: Int,
) : OutgoingGameMessage {
    override val category: ServerProtCategory
        get() = GameServerProtCategory.LOW_PRIORITY_PROT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CamSkybox

        return model == other.model
    }

    override fun hashCode(): Int {
        return model.hashCode()
    }

    override fun toString(): String =
        "CamSkybox(" +
            "model=$model" +
            ")"
}
