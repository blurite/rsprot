package net.rsprot.protocol.game.incoming

import net.rsprot.protocol.message.IncomingMessage
import net.rsprot.protocol.message.toIntOrMinusOne
import net.rsprot.protocol.util.CombinedId

/**
 * If button drag events are sent whenever an obj is dragged from one subcomponent
 * to another.
 * @property sourceInterfaceId the interface id from which the obj is dragged
 * @property sourceComponentId the component on that source interface from which
 * the obj is dragged
 * @property sourceSub the subcomponent from which the obj is dragged,
 * or -1 if none exists
 * @property sourceObj the obj that is being dragged, or -1 if none exists
 * @property targetInterfaceId the interface id to which the obj is being dragged
 * @property targetComponentId the component of the target interface to which
 * the obj is being dragged
 * @property targetSub the subcomponent of the target to which the obj is being dragged,
 * or -1 if none exists
 * @property targetObj the obj in that subcomponent which is being dragged on,
 * or -1 if there is no obj in the target position
 */
@Suppress("DuplicatedCode", "MemberVisibilityCanBePrivate")
public class IfButtonDEvent private constructor(
    private val sourceCombinedId: CombinedId,
    private val _sourceSub: UShort,
    private val _sourceObj: UShort,
    private val targetCombinedId: CombinedId,
    private val _targetSub: UShort,
    private val _targetObj: UShort,
) : IncomingMessage {
    public constructor(
        sourceCombinedId: CombinedId,
        sourceSub: Int,
        sourceObj: Int,
        targetCombinedId: CombinedId,
        targetSub: Int,
        targetObj: Int,
    ) : this(
        sourceCombinedId,
        sourceSub.toUShort(),
        sourceObj.toUShort(),
        targetCombinedId,
        targetSub.toUShort(),
        targetObj.toUShort(),
    )

    public val sourceInterfaceId: Int
        get() = sourceCombinedId.interfaceId
    public val sourceComponentId: Int
        get() = sourceCombinedId.componentId
    public val sourceSub: Int
        get() = _sourceSub.toIntOrMinusOne()
    public val sourceObj: Int
        get() = _sourceObj.toIntOrMinusOne()
    public val targetInterfaceId: Int
        get() = targetCombinedId.interfaceId
    public val targetComponentId: Int
        get() = targetCombinedId.componentId
    public val targetSub: Int
        get() = _targetSub.toIntOrMinusOne()
    public val targetObj: Int
        get() = _targetObj.toIntOrMinusOne()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IfButtonDEvent

        if (sourceCombinedId != other.sourceCombinedId) return false
        if (_sourceSub != other._sourceSub) return false
        if (_sourceObj != other._sourceObj) return false
        if (targetCombinedId != other.targetCombinedId) return false
        if (_targetSub != other._targetSub) return false
        if (_targetObj != other._targetObj) return false

        return true
    }

    override fun hashCode(): Int {
        var result = sourceCombinedId.hashCode()
        result = 31 * result + _sourceSub.hashCode()
        result = 31 * result + _sourceObj.hashCode()
        result = 31 * result + targetCombinedId.hashCode()
        result = 31 * result + _targetSub.hashCode()
        result = 31 * result + _targetObj.hashCode()
        return result
    }

    override fun toString(): String {
        return "IfButtonD(" +
            "sourceInterfaceId=$sourceInterfaceId, " +
            "sourceComponentId=$sourceComponentId, " +
            "sourceSub=$sourceSub, " +
            "sourceObj=$sourceObj, " +
            "targetInterfaceId=$targetInterfaceId, " +
            "targetComponentId=$targetComponentId, " +
            "targetSub=$targetSub, " +
            "targetObj=$targetObj" +
            ")"
    }
}
