package net.rsprot.protocol.loginprot.incoming

import net.rsprot.protocol.message.IncomingMessage

public data class ProofOfWorkReply(public val result: Long) : IncomingMessage
