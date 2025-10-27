package net.rsprot.protocol.game.outgoing.interfaces

import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.game.outgoing.GameServerProtCategory
import net.rsprot.protocol.message.OutgoingGameMessage
import net.rsprot.protocol.message.toIntOrMinusOne
import net.rsprot.protocol.util.CombinedId

/**
 * If set-npc-head is used to set a npc's chathead on an interface, commonly
 * in dialogues.
 * @property combinedId the bitpacked combination of [interfaceId] and [componentId].
 * @property interfaceId the interface id on which the model resides
 * @property componentId the component id on which the model resides
 * @property npc the id of the npc config whose head to set as the model
 */
public class IfSetNpcHead private constructor(
    public val combinedId: Int,
    private val _npc: UShort,
) : OutgoingGameMessage {
    public constructor(
        interfaceId: Int,
        componentId: Int,
        npc: Int,
    ) : this(
        CombinedId(interfaceId, componentId).combinedId,
        npc.toUShort(),
    )

    public constructor(
        combinedId: Int,
        npc: Int,
    ) : this(
        combinedId,
        npc.toUShort(),
    )

    private val _combinedId: CombinedId
        get() = CombinedId(combinedId)
    public val interfaceId: Int
        get() = _combinedId.interfaceId
    public val componentId: Int
        get() = _combinedId.componentId
    public val npc: Int
        get() = _npc.toIntOrMinusOne()
    override val category: ServerProtCategory
        get() = GameServerProtCategory.LOW_PRIORITY_PROT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IfSetNpcHead

        if (combinedId != other.combinedId) return false
        if (_npc != other._npc) return false

        return true
    }

    override fun hashCode(): Int {
        var result = combinedId.hashCode()
        result = 31 * result + _npc.hashCode()
        return result
    }

    override fun toString(): String =
        "IfSetNpcHead(" +
            "interfaceId=$interfaceId, " +
            "componentId=$componentId, " +
            "npc=$npc" +
            ")"
}
