package net.rsprot.protocol.game.incoming

import net.rsprot.protocol.message.IncomingMessage
import net.rsprot.protocol.message.toIntOrMinusOne
import net.rsprot.protocol.util.CombinedId

/**
 * If button target events are used whenever one button is targeted against another.
 * @property sourceInterfaceId the source interface id of the component that is being used
 * @property sourceComponentId the source component id that is being used
 * @property sourceSub the subcomponent id of the source, or -1 if none exists
 * @property sourceObj the obj in the source subcomponent, or -1 if none exists
 * @property targetInterfaceId the target interface id on which the source component
 * is being used
 * @property targetComponentId the target component id on which the source component
 * is being used
 * @property targetSub the target subcomponent id on which the source component is
 * being used, or -1 if none exists
 * @property targetObj the obj within the target subcomponent, or -1 if none exists.
 */
@Suppress("DuplicatedCode", "MemberVisibilityCanBePrivate")
public class IfButtonTEvent private constructor(
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

        other as IfButtonTEvent

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
        return "IfButtonT(" +
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
