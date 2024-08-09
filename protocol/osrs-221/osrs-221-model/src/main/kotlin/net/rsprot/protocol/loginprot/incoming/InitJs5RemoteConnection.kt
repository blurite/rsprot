package net.rsprot.protocol.loginprot.incoming

import net.rsprot.protocol.message.IncomingLoginMessage

public data class InitJs5RemoteConnection(
    val revision: Int,
) : IncomingLoginMessage
