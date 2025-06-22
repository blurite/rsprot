package net.rsprot.protocol.game.outgoing.sound

import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.game.outgoing.GameServerProtCategory
import net.rsprot.protocol.message.OutgoingGameMessage

/**
 * Synth sound is used to play a short sound effect locally for the given player.
 * @property id the id of the sound effect to play
 * @property loops the number of times to loop the sound effect
 * @property delay the delay in client cycles (20ms/cc) until the sound effect begins playing
 */
public class SynthSound private constructor(
    private val _id: UShort,
    private val _loops: UByte,
    private val _delay: UShort,
) : OutgoingGameMessage {
    public constructor(
        id: Int,
        loops: Int,
        delay: Int,
    ) : this(
        id.toUShort(),
        loops.toUByte(),
        delay.toUShort(),
    )

    public val id: Int
        get() = _id.toInt()
    public val loops: Int
        get() = _loops.toInt()
    public val delay: Int
        get() = _delay.toInt()
    override val category: ServerProtCategory
        get() = GameServerProtCategory.LOW_PRIORITY_PROT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SynthSound

        if (_id != other._id) return false
        if (_loops != other._loops) return false
        if (_delay != other._delay) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _id.hashCode()
        result = 31 * result + _loops.hashCode()
        result = 31 * result + _delay.hashCode()
        return result
    }

    override fun toString(): String =
        "SynthSound(" +
            "id=$id, " +
            "loops=$loops, " +
            "delay=$delay" +
            ")"
}
