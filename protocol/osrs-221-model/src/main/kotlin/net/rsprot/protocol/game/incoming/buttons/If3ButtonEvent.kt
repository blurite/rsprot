package net.rsprot.protocol.game.incoming.buttons

import net.rsprot.protocol.message.IncomingMessage
import net.rsprot.protocol.message.toIntOrMinusOne
import net.rsprot.protocol.util.CombinedId

/**
 * If3 button events are sent whenever a player clicks on a newer
 * if3-type component.
 * @property interfaceId the interface id the player interacted with
 * @property componentId the component id on that interface the player interacted with
 * @property sub the subcomponent within that component if it has one, otherwise -1
 * @property obj the obj in that subcomponent, or -1
 * @property op the option clicked, ranging from 1 to 10
 */
@Suppress("MemberVisibilityCanBePrivate")
public class If3ButtonEvent private constructor(
    private val combinedId: CombinedId,
    private val _sub: UShort,
    private val _obj: UShort,
    private val _op: UByte,
) : IncomingMessage {
    public constructor(
        combinedId: CombinedId,
        sub: Int,
        obj: Int,
        op: Int,
    ) : this(
        combinedId,
        sub.toUShort(),
        obj.toUShort(),
        op.toUByte(),
    )

    public val interfaceId: Int
        get() = combinedId.interfaceId
    public val componentId: Int
        get() = combinedId.componentId
    public val sub: Int
        get() = _sub.toIntOrMinusOne()
    public val obj: Int
        get() = _obj.toIntOrMinusOne()
    public val op: Int
        get() = _op.toInt()

    @Suppress("DuplicatedCode")
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as If3ButtonEvent

        if (combinedId != other.combinedId) return false
        if (_sub != other._sub) return false
        if (_obj != other._obj) return false
        if (_op != other._op) return false

        return true
    }

    override fun hashCode(): Int {
        var result = combinedId.hashCode()
        result = 31 * result + _sub.hashCode()
        result = 31 * result + _obj.hashCode()
        result = 31 * result + _op.hashCode()
        return result
    }

    override fun toString(): String {
        return "If3ButtonEvent(" +
            "interfaceId=$interfaceId, " +
            "componentId=$componentId, " +
            "sub=$sub, " +
            "obj=$obj, " +
            "op=$op" +
            ")"
    }
}
