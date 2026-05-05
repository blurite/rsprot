package net.rsprot.protocol.game.outgoing.sound

import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.game.outgoing.GameServerProtCategory
import net.rsprot.protocol.message.OutgoingGameMessage

/**
 * Ambience stop is used to stop any looping background ambience sound effect.
 * @property fade whether to fade the existing ambience out, if one is currently playing.
 */
public class AmbienceStop(
    public val fade: Boolean,
) : OutgoingGameMessage {
    override val category: ServerProtCategory
        get() = GameServerProtCategory.LOW_PRIORITY_PROT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AmbienceStop

        if (fade != other.fade) return false

        return true
    }

    override fun hashCode(): Int {
        return fade.hashCode()
    }

    override fun toString(): String =
        "AmbienceStop(" +
            "fade=$fade" +
            ")"
}
