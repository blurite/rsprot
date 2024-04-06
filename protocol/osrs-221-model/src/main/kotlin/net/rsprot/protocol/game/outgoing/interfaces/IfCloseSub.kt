package net.rsprot.protocol.game.outgoing.interfaces

import net.rsprot.protocol.message.OutgoingMessage
import net.rsprot.protocol.util.CombinedId

/**
 * If close-sub messages are used to close sub-level interfaces.
 * @property interfaceId the interface on which the sub-level interface is opened
 * @property componentId the component on which the sub-level interface is opened
 */
public class IfCloseSub private constructor(
    public val combinedId: CombinedId,
) : OutgoingMessage {
    public constructor(
        interfaceId: Int,
        componentId: Int,
    ) : this(
        CombinedId(interfaceId, componentId),
    )

    public val interfaceId: Int
        get() = combinedId.interfaceId
    public val componentId: Int
        get() = combinedId.componentId

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IfCloseSub

        return combinedId == other.combinedId
    }

    override fun hashCode(): Int {
        return combinedId.hashCode()
    }

    override fun toString(): String {
        return "IfCloseSub(" +
            "interfaceId=$interfaceId, " +
            "componentId=$componentId" +
            ")"
    }
}
