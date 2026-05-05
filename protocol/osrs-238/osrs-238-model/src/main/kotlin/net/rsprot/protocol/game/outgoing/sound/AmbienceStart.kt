package net.rsprot.protocol.game.outgoing.sound

import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.game.outgoing.GameServerProtCategory
import net.rsprot.protocol.message.OutgoingGameMessage

/**
 * Ambience start is used to set a looping background ambience sound effect.
 * This will continue looping until stopped or overwritten.
 * @property id the id of the sound effect to use for the ambience.
 * @property fade whether to fade the existing ambience out, if one is currently playing.
 */
public class AmbienceStart private constructor(
    private val _id: UShort,
    public val fade: Boolean,
) : OutgoingGameMessage {
    public constructor(
        id: Int,
        fade: Boolean,
    ) : this(
        id.toUShort(),
        fade,
    )

    public val id: Int
        get() = _id.toInt()
    override val category: ServerProtCategory
        get() = GameServerProtCategory.LOW_PRIORITY_PROT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AmbienceStart

        if (_id != other._id) return false
        if (fade != other.fade) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _id.hashCode()
        result = 31 * result + fade.hashCode()
        return result
    }

    override fun toString(): String =
        "AmbienceStart(" +
            "id=$id, " +
            "fade=$fade" +
            ")"
}
