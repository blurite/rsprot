package net.rsprot.protocol.js5.incoming

import net.rsprot.protocol.message.IncomingJs5Message

public data class XorChange(
    public val key: Int,
) : IncomingJs5Message
