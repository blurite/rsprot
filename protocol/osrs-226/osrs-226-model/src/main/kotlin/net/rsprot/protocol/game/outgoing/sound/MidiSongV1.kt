package net.rsprot.protocol.game.outgoing.sound

import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.game.outgoing.GameServerProtCategory
import net.rsprot.protocol.message.OutgoingGameMessage

/**
 * Midi song V1 packet is used to play a midi song, in the old format.
 * This is equal to playing [MidiSongV2] with the arguments of `id, 0, 60, 60, 0`.
 * @property id the id of the song to play
 */
public class MidiSongV1(
    public val id: Int,
) : OutgoingGameMessage {
    override val category: ServerProtCategory
        get() = GameServerProtCategory.LOW_PRIORITY_PROT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MidiSongV1

        return id == other.id
    }

    override fun hashCode(): Int = id

    override fun toString(): String = "MidiSongV1(id=$id)"
}
