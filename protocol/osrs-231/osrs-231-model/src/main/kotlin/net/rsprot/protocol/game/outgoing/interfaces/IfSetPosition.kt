package net.rsprot.protocol.game.outgoing.interfaces

import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.game.outgoing.GameServerProtCategory
import net.rsprot.protocol.message.OutgoingGameMessage
import net.rsprot.protocol.util.CombinedId

/**
 * If set-position events are used to move a component on an interface.
 * @property combinedId the bitpacked combination of [interfaceId] and [componentId].
 * @property interfaceId the interface on which the component to move exists
 * @property componentId the component id to move
 * @property x the x coordinate to move to
 * @property y the y coordinate to move to
 */
public class IfSetPosition private constructor(
    public val combinedId: Int,
    private val _x: UShort,
    private val _y: UShort,
) : OutgoingGameMessage {
    public constructor(
        interfaceId: Int,
        componentId: Int,
        x: Int,
        y: Int,
    ) : this(
        CombinedId(interfaceId, componentId).combinedId,
        x.toUShort(),
        y.toUShort(),
    )

    public constructor(
        combinedId: Int,
        x: Int,
        y: Int,
    ) : this(
        combinedId,
        x.toUShort(),
        y.toUShort(),
    )

    private val _combinedId: CombinedId
        get() = CombinedId(combinedId)
    public val interfaceId: Int
        get() = _combinedId.interfaceId
    public val componentId: Int
        get() = _combinedId.componentId
    public val x: Int
        get() = _x.toInt()
    public val y: Int
        get() = _y.toInt()
    override val category: ServerProtCategory
        get() = GameServerProtCategory.LOW_PRIORITY_PROT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IfSetPosition

        if (combinedId != other.combinedId) return false
        if (_x != other._x) return false
        if (_y != other._y) return false

        return true
    }

    override fun hashCode(): Int {
        var result = combinedId.hashCode()
        result = 31 * result + _x.hashCode()
        result = 31 * result + _y.hashCode()
        return result
    }

    override fun toString(): String =
        "IfSetPosition(" +
            "interfaceId=$interfaceId, " +
            "componentId=$componentId, " +
            "x=$x, " +
            "y=$y" +
            ")"
}
