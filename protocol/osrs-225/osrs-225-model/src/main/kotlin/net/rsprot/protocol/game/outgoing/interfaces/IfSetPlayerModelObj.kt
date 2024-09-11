package net.rsprot.protocol.game.outgoing.interfaces

import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.game.outgoing.GameServerProtCategory
import net.rsprot.protocol.message.OutgoingGameMessage
import net.rsprot.protocol.util.CombinedId

/**
 * If setplayermodel obj is used to set a worn obj on a player model.
 * @property combinedId the bitpacked combination of [interfaceId] and [componentId].
 * @property interfaceId the id of the interface on which the model resides
 * @property componentId the id of the component on which the model resides
 * @property obj the id of the obj. Interestingly, the client reads a 32-bit int
 * for the obj, even though configs having a strict 32767/65535 limitation elsewhere
 * in the client.
 */
public class IfSetPlayerModelObj(
    public val combinedId: Int,
    public val obj: Int,
) : OutgoingGameMessage {
    public constructor(
        interfaceId: Int,
        componentId: Int,
        obj: Int,
    ) : this(
        CombinedId(interfaceId, componentId).combinedId,
        obj,
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

        other as IfSetPlayerModelObj

        if (combinedId != other.combinedId) return false
        if (obj != other.obj) return false

        return true
    }

    override fun hashCode(): Int {
        var result = combinedId.hashCode()
        result = 31 * result + obj
        return result
    }

    override fun toString(): String =
        "IfSetPlayerModelObj(" +
            "interfaceId=$interfaceId, " +
            "componentId=$componentId, " +
            "obj=$obj" +
            ")"
}
