@file:Suppress("MemberVisibilityCanBePrivate")

package net.rsprot.protocol.loginprot.incoming

import net.rsprot.protocol.loginprot.incoming.util.AuthenticationType
import net.rsprot.protocol.loginprot.incoming.util.LoginBlock
import net.rsprot.protocol.message.IncomingMessage

public data class GameLogin(
    public val block: LoginBlock<AuthenticationType<*>>,
) : IncomingMessage
