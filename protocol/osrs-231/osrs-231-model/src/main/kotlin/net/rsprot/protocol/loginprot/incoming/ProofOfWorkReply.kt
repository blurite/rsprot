package net.rsprot.protocol.loginprot.incoming

import net.rsprot.protocol.message.IncomingLoginMessage

public data class ProofOfWorkReply(
    public val result: Long,
) : IncomingLoginMessage
