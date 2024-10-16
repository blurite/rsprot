package net.rsprot.protocol.game.outgoing.interfaces

import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.game.outgoing.GameServerProtCategory
import net.rsprot.protocol.message.OutgoingGameMessage
import net.rsprot.protocol.util.CombinedId

/**
 * If set scroll pos messages are used to force the scroll position
 * of a layer component.
 * @property combinedId the bitpacked combination of [interfaceId] and [componentId].
 * @property interfaceId the interface on which the scroll layer exists
 * @property componentId the component id of the scroll layer
 * @property scrollPos the scroll position to set to
 */
public class IfSetScrollPos private constructor(
    public val combinedId: Int,
    private val _scrollPos: UShort,
) : OutgoingGameMessage {
    public constructor(
        interfaceId: Int,
        componentId: Int,
        scrollPos: Int,
    ) : this(
        CombinedId(interfaceId, componentId).combinedId,
        scrollPos.toUShort(),
    )

    public constructor(
        combinedId: Int,
        scrollPos: Int,
    ) : this(
        combinedId,
        scrollPos.toUShort(),
    )

    private val _combinedId: CombinedId
        get() = CombinedId(combinedId)
    public val interfaceId: Int
        get() = _combinedId.interfaceId
    public val componentId: Int
        get() = _combinedId.componentId
    public val scrollPos: Int
        get() = _scrollPos.toInt()
    override val category: ServerProtCategory
        get() = GameServerProtCategory.LOW_PRIORITY_PROT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IfSetScrollPos

        if (combinedId != other.combinedId) return false
        if (_scrollPos != other._scrollPos) return false

        return true
    }

    override fun hashCode(): Int {
        var result = combinedId.hashCode()
        result = 31 * result + _scrollPos.hashCode()
        return result
    }

    override fun toString(): String =
        "IfSetScrollPos(" +
            "interfaceId=$interfaceId, " +
            "componentId=$componentId, " +
            "scrollPos=$scrollPos" +
            ")"
}
