package net.rsprot.protocol.js5.incoming

import net.rsprot.protocol.message.IncomingMessage

public data class XorChange(
    private val key: Int,
) : IncomingMessage
