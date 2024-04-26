package net.rsprot.protocol.js5.incoming

import net.rsprot.protocol.message.IncomingJs5Message

public data class UrgentRequest(
    private val _archiveId: UByte,
    private val _groupId: UShort,
) : IncomingJs5Message, Js5GroupRequest {
    override val archiveId: Int
        get() = _archiveId.toInt()
    override val groupId: Int
        get() = _groupId.toInt()
}
