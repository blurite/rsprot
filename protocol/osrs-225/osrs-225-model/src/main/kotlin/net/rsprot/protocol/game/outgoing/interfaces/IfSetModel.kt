package net.rsprot.protocol.game.outgoing.interfaces

import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.game.outgoing.GameServerProtCategory
import net.rsprot.protocol.message.OutgoingGameMessage
import net.rsprot.protocol.util.CombinedId

/**
 * If set model packet is used to set a model to render on an interface.
 * The component must be of model type for this to succeed.
 * @property combinedId the bitpacked combination of [interfaceId] and [componentId].
 * @property interfaceId the interface id on which to set the events
 * @property componentId the component on that interface to set the events on
 * @property model the id of the model to render.
 */
public class IfSetModel private constructor(
    public val combinedId: Int,
    private val _model: UShort,
) : OutgoingGameMessage {
    public constructor(
        interfaceId: Int,
        componentId: Int,
        model: Int,
    ) : this(
        CombinedId(interfaceId, componentId).combinedId,
        model.toUShort(),
    )

    public constructor(
        combinedId: Int,
        model: Int,
    ) : this(
        combinedId,
        model.toUShort(),
    )

    private val _combinedId: CombinedId
        get() = CombinedId(combinedId)
    public val interfaceId: Int
        get() = _combinedId.interfaceId
    public val componentId: Int
        get() = _combinedId.componentId
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

    override fun toString(): String =
        "IfSetModel(" +
            "interfaceId=$interfaceId, " +
            "componentId=$componentId, " +
            "model=$model" +
            ")"
}
