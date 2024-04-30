package net.rsprot.protocol.game.outgoing.interfaces

import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.game.incoming.GameServerProtCategory
import net.rsprot.protocol.message.OutgoingGameMessage
import net.rsprot.protocol.util.CombinedId

public class IfSetModel private constructor(
    public val combinedId: CombinedId,
    private val _model: UShort,
) : OutgoingGameMessage {
    public constructor(
        interfaceId: Int,
        componentId: Int,
        model: Int,
    ) : this(
        CombinedId(interfaceId, componentId),
        model.toUShort(),
    )

    public val interfaceId: Int
        get() = combinedId.interfaceId
    public val componentId: Int
        get() = combinedId.componentId
    public val model: Int
        get() = _model.toInt()
    override val category: ServerProtCategory
        get() = GameServerProtCategory.LOW_PRIORITY_PROT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IfSetModel

        if (combinedId != other.combinedId) return false
        if (_model != other._model) return false

        return true
    }

    override fun hashCode(): Int {
        var result = combinedId.hashCode()
        result = 31 * result + _model.hashCode()
        return result
    }

    override fun toString(): String {
        return "IfSetModel(" +
            "interfaceId=$interfaceId, " +
            "componentId=$componentId, " +
            "model=$model" +
            ")"
    }
}
