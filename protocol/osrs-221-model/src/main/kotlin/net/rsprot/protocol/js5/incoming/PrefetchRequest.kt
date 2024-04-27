package net.rsprot.protocol.js5.incoming

import net.rsprot.protocol.message.IncomingJs5Message

public class PrefetchRequest(
    private val _archiveId: UByte,
    private val _groupId: UShort,
) : IncomingJs5Message, Js5GroupRequest {
    override val archiveId: Int
        get() = _archiveId.toInt()
    override val groupId: Int
        get() = _groupId.toInt()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PrefetchRequest) return false

        if (_archiveId != other._archiveId) return false
        if (_groupId != other._groupId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _archiveId.hashCode()
        result = 31 * result + _groupId.hashCode()
        return result
    }

    override fun toString(): String {
        return "PrefetchRequest(" +
            "archiveId=$archiveId, " +
            "groupId=$groupId" +
            ")"
    }
}
