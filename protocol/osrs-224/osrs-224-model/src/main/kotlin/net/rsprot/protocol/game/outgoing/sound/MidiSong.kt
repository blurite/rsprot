package net.rsprot.protocol.game.outgoing.sound

import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.game.outgoing.GameServerProtCategory
import net.rsprot.protocol.message.OutgoingGameMessage

/**
 * Midi song packets are used to play songs through the music player.
 * @property id the id of the midi song
 * @property fadeOutDelay the delay in client cycles (20ms/cc) until the old song
 * begins fading out. The default value for this, based on the old midi song packet, is 0.
 * @property fadeOutSpeed the speed at which the old song fades out in client cycles (20ms/cc).
 * The default value for this, based on the old midi song packet, is 60.
 * @property fadeInDelay the delay until the new song begins playing, in client cycles (20ms/cc).
 * The default value for this, based on the old midi song packet is 60.
 * @property fadeInSpeed the speed at which the new song fades in, in client cycles (20ms/cc).
 * The default value for this, based on the old midi song packet is 0.
 */
@Suppress("DuplicatedCode")
public class MidiSong private constructor(
    private val _id: UShort,
    private val _fadeOutDelay: UShort,
    private val _fadeOutSpeed: UShort,
    private val _fadeInDelay: UShort,
    private val _fadeInSpeed: UShort,
) : OutgoingGameMessage {
    public constructor(
        id: Int,
        fadeOutDelay: Int,
        fadeOutSpeed: Int,
        fadeInDelay: Int,
        fadeInSpeed: Int,
    ) : this(
        id.toUShort(),
        fadeOutDelay.toUShort(),
        fadeOutSpeed.toUShort(),
        fadeInDelay.toUShort(),
        fadeInSpeed.toUShort(),
    )

    public val id: Int
        get() = _id.toInt()
    public val fadeOutDelay: Int
        get() = _fadeOutDelay.toInt()
    public val fadeOutSpeed: Int
        get() = _fadeOutSpeed.toInt()
    public val fadeInDelay: Int
        get() = _fadeInDelay.toInt()
    public val fadeInSpeed: Int
        get() = _fadeInSpeed.toInt()
    override val category: ServerProtCategory
        get() = GameServerProtCategory.LOW_PRIORITY_PROT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MidiSong

        if (_id != other._id) return false
        if (_fadeOutDelay != other._fadeOutDelay) return false
        if (_fadeOutSpeed != other._fadeOutSpeed) return false
        if (_fadeInDelay != other._fadeInDelay) return false
        if (_fadeInSpeed != other._fadeInSpeed) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _id.hashCode()
        result = 31 * result + _fadeOutDelay.hashCode()
        result = 31 * result + _fadeOutSpeed.hashCode()
        result = 31 * result + _fadeInDelay.hashCode()
        result = 31 * result + _fadeInSpeed.hashCode()
        return result
    }

    override fun toString(): String =
        "MidiSong(" +
            "id=$id, " +
            "fadeOutDelay=$fadeOutDelay, " +
            "fadeOutSpeed=$fadeOutSpeed, " +
            "fadeInDelay=$fadeInDelay, " +
            "fadeInSpeed=$fadeInSpeed" +
            ")"
}
