package net.rsprot.protocol.game.outgoing.interfaces

import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.game.outgoing.GameServerProtCategory
import net.rsprot.protocol.message.OutgoingGameMessage
import net.rsprot.protocol.util.CombinedId

/**
 * If set-hide is used to hide or unhide a component and its children on an interface.
 * @property combinedId the bitpacked combination of [interfaceId] and [componentId].
 * @property interfaceId the interface id on which the component to hide or unhide resides on
 * @property componentId the component on the [interfaceId] to hide or unhide
 * @property hidden whether to hide or unhide the component
 */
public class IfSetHide(
    public val combinedId: Int,
    public val hidden: Boolean,
) : OutgoingGameMessage {
    public constructor(
        interfaceId: Int,
        componentId: Int,
        hidden: Boolean,
    ) : this(
        CombinedId(interfaceId, componentId).combinedId,
        hidden,
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

        other as IfSetHide

        if (combinedId != other.combinedId) return false
        if (hidden != other.hidden) return false

        return true
    }

    override fun hashCode(): Int {
        var result = combinedId.hashCode()
        result = 31 * result + hidden.hashCode()
        return result
    }

    override fun toString(): String =
        "IfSetHide(" +
            "interfaceId=$interfaceId, " +
            "componentId=$componentId, " +
            "hidden=$hidden" +
            ")"
}
