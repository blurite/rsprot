package net.rsprot.protocol.game.outgoing.varp

import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.game.outgoing.GameServerProtCategory
import net.rsprot.protocol.message.OutgoingGameMessage

/**
 * Varp small messages are used to send a varp to the client that
 * has a value which does not fit in the range of a byte, being -128..127.
 * For values which do fit in the aforementioned range, the [VarpSmall]
 * packed is preferred as it takes up less bandwidth, although nothing
 * prevents one from sending all varps using this variant.
 * @property id the id of the varp
 * @property value the value of the varp
 */
public class VarpLarge private constructor(
    private val _id: UShort,
    public val value: Int,
) : OutgoingGameMessage {
    public constructor(
        id: Int,
        value: Int,
    ) : this(
        id.toUShort(),
        value,
    )

    public val id: Int
        get() = _id.toInt()
    override val category: ServerProtCategory
        get() = GameServerProtCategory.HIGH_PRIORITY_PROT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as VarpLarge

        if (_id != other._id) return false
        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _id.hashCode()
        result = 31 * result + value
        return result
    }

    override fun toString(): String =
        "VarpLarge(" +
            "id=$id, " +
            "value=$value" +
            ")"
}
