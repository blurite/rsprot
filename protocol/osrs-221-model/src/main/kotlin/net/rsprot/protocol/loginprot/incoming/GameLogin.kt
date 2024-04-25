package net.rsprot.protocol.loginprot.incoming

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.loginprot.incoming.util.AuthenticationType
import net.rsprot.protocol.loginprot.incoming.util.LoginBlock
import net.rsprot.protocol.message.IncomingMessage
import java.util.function.Function

public data class GameLogin(
    public val buffer: JagByteBuf,
    public val decoder: Function<JagByteBuf, LoginBlock<AuthenticationType<*>>>,
) : IncomingMessage
