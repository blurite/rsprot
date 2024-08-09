package net.rsprot.protocol.loginprot.incoming

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.loginprot.incoming.util.AuthenticationType
import net.rsprot.protocol.loginprot.incoming.util.LoginBlockDecodingFunction
import net.rsprot.protocol.message.IncomingLoginMessage

public typealias LoginDecodingFunction = LoginBlockDecodingFunction<AuthenticationType<*>>

public data class GameLogin(
    public val buffer: JagByteBuf,
    public val decoder: LoginDecodingFunction,
) : IncomingLoginMessage
