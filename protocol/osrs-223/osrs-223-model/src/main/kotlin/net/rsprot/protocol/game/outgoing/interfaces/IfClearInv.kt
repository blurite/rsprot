package net.rsprot.protocol.game.outgoing.interfaces

import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.game.outgoing.GameServerProtCategory
import net.rsprot.protocol.message.OutgoingGameMessage
import net.rsprot.protocol.util.CombinedId

/**
 * If clear-inv messaged are used to clear all objs on any if-1 type
 * component. As there are very few if-1 type old interfaces remaining,
 * this packet is mostly unused nowadays.
 * @property combinedId the bitpacked combination of [interfaceId] and [componentId].
 * @property interfaceId the id of the interface on which the inv exists
 * @property componentId the id of the component on the [interfaceId] to be cleared
 */
public class IfClearInv(
    public val combinedId: Int,
) : OutgoingGameMessage {
    public constructor(
        interfaceId: Int,
        componentId: Int,
    ) : this(
        CombinedId(interfaceId, componentId).combinedId,
    )

    private val _combinedId: CombinedId
        get() = CombinedId(combinedId)
    public val interfaceId: Int
        get() = _combinedId.interfaceId
    public val componentId: Int
        get() = _combinedId.componentId
    override val category: ServerProtCategory
        get() = GameServerProtCategory.LOW_PRIORITY_PROT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IfClearInv

        return _combinedId == other._combinedId
    }

    override fun hashCode(): Int = _combinedId.hashCode()

    override fun toString(): String =
        "IfClearInv(" +
            "interfaceId=$interfaceId, " +
            "componentId=$componentId" +
            ")"
}
