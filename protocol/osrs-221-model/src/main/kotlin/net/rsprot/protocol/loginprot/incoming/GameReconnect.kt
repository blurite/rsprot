package net.rsprot.protocol.loginprot.incoming

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.util.XteaKey
import net.rsprot.protocol.loginprot.incoming.util.LoginBlock
import net.rsprot.protocol.message.IncomingMessage
import java.util.function.Function

public data class GameReconnect(
    public val buffer: JagByteBuf,
    public val decoder: Function<JagByteBuf, LoginBlock<XteaKey>>,
) : IncomingMessage
