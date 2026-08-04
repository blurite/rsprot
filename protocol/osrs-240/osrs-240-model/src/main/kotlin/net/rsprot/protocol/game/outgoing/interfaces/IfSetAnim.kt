package net.rsprot.protocol.game.outgoing.interfaces

import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.game.outgoing.GameServerProtCategory
import net.rsprot.protocol.message.OutgoingGameMessage
import net.rsprot.protocol.message.toIntOrMinusOne
import net.rsprot.protocol.util.CombinedId

/**
 * If set-anim is used to make a model animate on a component.
 * @property combinedId the bitpacked combination of [interfaceId] and [componentId].
 * @property interfaceId the id of the interface on which the model resides
 * @property componentId the id of the component on which the model resides
 * @property anim the id of the animation to play, or -1 to reset the animation
 */
public class IfSetAnim private constructor(
    public val combinedId: Int,
    private val _anim: UShort,
) : OutgoingGameMessage {
    public constructor(
        interfaceId: Int,
        componentId: Int,
        anim: Int,
    ) : this(
        CombinedId(interfaceId, componentId).combinedId,
        anim.toUShort(),
    )

    public constructor(
        combinedId: Int,
        anim: Int,
    ) : this(
        combinedId,
        anim.toUShort(),
    )

    private val _combinedId: CombinedId
        get() = CombinedId(combinedId)
    public val interfaceId: Int
        get() = _combinedId.interfaceId
    public val componentId: Int
        get() = _combinedId.componentId
    public val anim: Int
        get() = _anim.toIntOrMinusOne()
    override val category: ServerProtCategory
        get() = GameServerProtCategory.LOW_PRIORITY_PROT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IfSetAnim

        if (combinedId != other.combinedId) return false
        if (_anim != other._anim) return false

        return true
    }

    override fun hashCode(): Int {
        var result = combinedId.hashCode()
        result = 31 * result + _anim.hashCode()
        return result
    }

    override fun toString(): String =
        "IfSetAnim(" +
            "interfaceId=$interfaceId, " +
            "componentId=$componentId, " +
            "anim=$anim" +
            ")"
}
