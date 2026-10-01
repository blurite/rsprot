package net.rsprot.protocol.game.outgoing.group.util

import net.rsprot.protocol.game.outgoing.group.util.GroupVariable.IntGroupVariable
import net.rsprot.protocol.game.outgoing.group.util.GroupVariable.LongGroupVariable
import net.rsprot.protocol.game.outgoing.group.util.GroupVariable.StringGroupVariable

public class GroupVarUpdate<out T> private constructor(
    public val index: Int,
    public val packedGroupVar: Int,
    public val variable: GroupVariable<T>,
) {
    public constructor(
        index: Int,
        id: Int,
        isMember: Boolean,
        varIndex: Int,
        variable: GroupVariable<T>,
    ) : this(
        index,
        packGroupVar(id, isMember, variable, varIndex),
        variable,
    ) {
        require(index in 0..255)
    }

    public val id: Int
        get() = unpackId(packedGroupVar)
    public val isMember: Boolean
        get() = unpackMember(packedGroupVar)
    public val baseVarType: Int
        get() = unpackBaseVarType(packedGroupVar)
    public val varIndex: Int
        get() = unpackVarIndex(packedGroupVar)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GroupVarUpdate<T>

        if (index != other.index) return false
        if (packedGroupVar != other.packedGroupVar) return false
        if (variable != other.variable) return false

        return true
    }

    override fun hashCode(): Int {
        var result = index
        result = 31 * result + packedGroupVar
        result = 31 * result + variable.hashCode()
        return result
    }

    override fun toString(): String {
        return "GroupVarUpdate(" +
            "index=$index, " +
            "id=$id, " +
            "isMember=$isMember, " +
            "baseVarType=$baseVarType, " +
            "varIndex=$varIndex, " +
            "variable=$variable" +
            ")"
    }

    private companion object {
        private fun packGroupVar(
            id: Int,
            isMember: Boolean,
            variable: GroupVariable<*>,
            varIndex: Int,
        ): Int {
            require(varIndex in 0..65535)
            require(id in 0..2047)
            val baseVarType =
                when (variable) {
                    is IntGroupVariable -> 0
                    is LongGroupVariable -> 1
                    is StringGroupVariable -> 2
                }
            return (id shl 20)
                .or(if (isMember) (1 shl 18) else 0)
                .or(baseVarType shl 16)
                .or(varIndex)
        }

        private fun unpackId(packed: Int): Int {
            return packed ushr 20
        }

        private fun unpackMember(packed: Int): Boolean {
            return (packed ushr 18 and 0x1) != 0
        }

        private fun unpackBaseVarType(packed: Int): Int {
            return packed ushr 16 and 0x3
        }

        private fun unpackVarIndex(packed: Int): Int {
            return packed and 0xFFFF
        }
    }
}
