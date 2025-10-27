package net.rsprot.protocol.game.outgoing.interfaces

import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.game.outgoing.GameServerProtCategory
import net.rsprot.protocol.message.OutgoingGameMessage
import net.rsprot.protocol.util.CombinedId

/**
 * If set-rotate-speed packet is used to make a model rotate
 * according to the client's update counter. This only has an effect
 * on model-type components.
 * @property combinedId the bitpacked combination of [interfaceId] and [componentId].
 * @property interfaceId the id of the interface on which the component to rotate
 * lives.
 * @property componentId the component on which the model to rotate lives
 * @property xSpeed the speed of the x angle of the model to rotate by
 * each client cycle (20ms/cc), with a value of 1 being equal to 1/2048th of a
 * full circle
 * @property ySpeed the speed of the y angle of the model to rotate by
 * each client cycle (20ms/cc), with a value of 1 being equal to 1/2048th of a
 * full circle
 */
public class IfSetRotateSpeed private constructor(
    public val combinedId: Int,
    private val _xSpeed: UShort,
    private val _ySpeed: UShort,
) : OutgoingGameMessage {
    public constructor(
        interfaceId: Int,
        componentId: Int,
        xSpeed: Int,
        ySpeed: Int,
    ) : this(
        CombinedId(interfaceId, componentId).combinedId,
        xSpeed.toUShort(),
        ySpeed.toUShort(),
    )

    public constructor(
        combinedId: Int,
        xSpeed: Int,
        ySpeed: Int,
    ) : this(
        combinedId,
        xSpeed.toUShort(),
        ySpeed.toUShort(),
    )

    private val _combinedId: CombinedId
        get() = CombinedId(combinedId)
    public val interfaceId: Int
        get() = _combinedId.interfaceId
    public val componentId: Int
        get() = _combinedId.componentId
    public val xSpeed: Int
        get() = _xSpeed.toInt()
    public val ySpeed: Int
        get() = _ySpeed.toInt()
    override val category: ServerProtCategory
        get() = GameServerProtCategory.LOW_PRIORITY_PROT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IfSetRotateSpeed

        if (combinedId != other.combinedId) return false
        if (_xSpeed != other._xSpeed) return false
        if (_ySpeed != other._ySpeed) return false

        return true
    }

    override fun hashCode(): Int {
        var result = combinedId.hashCode()
        result = 31 * result + _xSpeed.hashCode()
        result = 31 * result + _ySpeed.hashCode()
        return result
    }

    override fun toString(): String =
        "IfSetRotateSpeed(" +
            "interfaceId=$interfaceId, " +
            "componentId=$componentId, " +
            "xSpeed=$xSpeed, " +
            "ySpeed=$ySpeed" +
            ")"
}
