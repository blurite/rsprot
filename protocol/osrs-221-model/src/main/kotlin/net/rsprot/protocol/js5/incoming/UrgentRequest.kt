package net.rsprot.protocol.js5.incoming

import net.rsprot.protocol.message.IncomingMessage

public data class UrgentRequest(
    private val _archiveId: UByte,
    private val _groupId: UShort,
) : IncomingMessage {
    public val archiveId: Int
        get() = _archiveId.toInt()
    public val groupId: Int
        get() = _groupId.toInt()
}
