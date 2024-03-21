package net.rsprot.protocol.js5.incoming

import net.rsprot.protocol.message.IncomingMessage

public data class PrefetchRequest(
    private val _archiveId: UByte,
    private val _groupId: UShort,
) : IncomingMessage, Js5GroupRequest {
    override val archiveId: Int
        get() = _archiveId.toInt()
    override val groupId: Int
        get() = _groupId.toInt()
}
