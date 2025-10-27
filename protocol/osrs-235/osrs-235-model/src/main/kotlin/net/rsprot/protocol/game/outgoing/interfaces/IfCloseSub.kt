package net.rsprot.protocol.game.outgoing.interfaces

import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.game.outgoing.GameServerProtCategory
import net.rsprot.protocol.message.OutgoingGameMessage
import net.rsprot.protocol.util.CombinedId

/**
 * If close-sub messages are used to close sub-level interfaces.
 * @property combinedId the bitpacked combination of [interfaceId] and [componentId].
 * @property interfaceId the interface on which the sub-level interface is opened
 * @property componentId the component on which the sub-level interface is opened
 */
public class IfCloseSub(
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

        other as IfCloseSub

        return _combinedId == other._combinedId
    }

    override fun hashCode(): Int = _combinedId.hashCode()

    override fun toString(): String =
        "IfCloseSub(" +
            "interfaceId=$interfaceId, " +
            "componentId=$componentId" +
            ")"
}
