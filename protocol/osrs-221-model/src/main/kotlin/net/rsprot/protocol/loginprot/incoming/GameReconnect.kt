package net.rsprot.protocol.loginprot.incoming

import net.rsprot.protocol.loginprot.incoming.util.LoginBlock
import net.rsprot.protocol.loginprot.incoming.util.XteaKey
import net.rsprot.protocol.message.IncomingMessage

public data class GameReconnect(
    public val block: LoginBlock<XteaKey>,
) : IncomingMessage
