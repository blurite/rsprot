package net.rsprot.protocol.js5.incoming

import net.rsprot.protocol.message.IncomingMessage

public data class XorChange(
    public val key: Int,
) : IncomingMessage
