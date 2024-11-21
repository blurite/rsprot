package net.rsprot.protocol.game.outgoing.specific

import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.game.outgoing.GameServerProtCategory
import net.rsprot.protocol.message.OutgoingGameMessage

/**
 * Npc anim specifics are used to play an animation on a NPC for a specific player,
 * and not the entire world.
 * @property index the index of the npc in the world
 * @property id the id of the animation
 * @property delay the delay of the animation before it begins playing in client cycles (20ms/cc)
 */
public class NpcAnimSpecific private constructor(
    private val _index: UShort,
    private val _id: UShort,
    private val _delay: UByte,
) : OutgoingGameMessage {
    public constructor(
        index: Int,
        id: Int,
        delay: Int,
    ) : this(
        index.toUShort(),
        id.toUShort(),
        delay.toUByte(),
    )

    public val index: Int
        get() = _index.toInt()
    public val id: Int
        get() = _id.toInt()
    public val delay: Int
        get() = _delay.toInt()
    override val category: ServerProtCategory
        get() = GameServerProtCategory.LOW_PRIORITY_PROT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NpcAnimSpecific

        if (_index != other._index) return false
        if (_id != other._id) return false
        if (_delay != other._delay) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _index.hashCode()
        result = 31 * result + _id.hashCode()
        result = 31 * result + _delay.hashCode()
        return result
    }

    override fun toString(): String =
        "NpcAnimSpecific(" +
            "index=$index, " +
            "id=$id, " +
            "delay=$delay" +
            ")"
}
