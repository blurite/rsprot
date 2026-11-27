package net.rsprot.protocol.game.outgoing.interfaces

import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.game.outgoing.GameServerProtCategory
import net.rsprot.protocol.message.OutgoingGameMessage
import net.rsprot.protocol.util.CombinedId

/**
 * If set-player-head is used to set the local player's chathead on an interface,
 * commonly used for dialogues.
 * @property combinedId the bitpacked combination of [interfaceId] and [componentId].
 * @property interfaceId the id of the interface on which the chathead model resides
 * @property componentId the id of the component on which the chathead model resides
 */
public class IfSetPlayerHead(
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

        other as IfSetPlayerHead

        return combinedId == other.combinedId
    }

    override fun hashCode(): Int = combinedId.hashCode()

    override fun toString(): String =
        "IfSetPlayerHead(" +
            "interfaceId=$interfaceId, " +
            "componentId=$componentId" +
            ")"
}
