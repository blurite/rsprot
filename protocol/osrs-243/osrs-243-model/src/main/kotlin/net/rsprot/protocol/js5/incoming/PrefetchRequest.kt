package net.rsprot.protocol.js5.incoming

import net.rsprot.protocol.message.IncomingJs5Message

public class PrefetchRequest(
    private val _archiveId: UByte,
    override val groupId: Int,
) : IncomingJs5Message,
    Js5GroupRequest {
    init {
        require(groupId in 0..0xFFFFFF)
    }

    override val archiveId: Int
        get() = _archiveId.toInt()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PrefetchRequest) return false

        if (_archiveId != other._archiveId) return false
        if (groupId != other.groupId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _archiveId.hashCode()
        result = 31 * result + groupId.hashCode()
        return result
    }

    override fun toString(): String =
        "PrefetchRequest(" +
            "archiveId=$archiveId, " +
            "groupId=$groupId" +
            ")"
}
