package net.rsprot.protocol.game.incoming.misc.client

import net.rsprot.protocol.ClientProtCategory
import net.rsprot.protocol.game.incoming.GameClientProtCategory
import net.rsprot.protocol.message.IncomingGameMessage

/**
 * RSeven Status packet is sent to inform the client of various RT7 related properties on login.
 * This packet is only sent on the native client.
 * @property packed the bitpacked flag containing RT7 properties.
 * As of revision 231.2, the very first bit is for 'force disable rseven' and the second bit is
 * always enabled. The other bits are currently not in use.
 */
@Suppress("MemberVisibilityCanBePrivate")
public class RSevenStatus private constructor(
    private val _packedValue: UByte,
) : IncomingGameMessage {
    public constructor(
        packedValue: Int,
    ) : this(
        packedValue.toUByte(),
    )

    public val packed: Int
        get() = _packedValue.toInt()
    override val category: ClientProtCategory
        get() = GameClientProtCategory.CLIENT_EVENT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RSevenStatus

        return _packedValue == other._packedValue
    }

    override fun hashCode(): Int {
        return _packedValue.hashCode()
    }

    override fun toString(): String {
        return "RSevenStatus(packed=$packed)"
    }
}
