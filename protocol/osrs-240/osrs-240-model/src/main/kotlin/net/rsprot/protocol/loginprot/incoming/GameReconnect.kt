package net.rsprot.protocol.loginprot.incoming

import net.rsprot.buffer.JagByteBuf
import net.rsprot.crypto.xtea.XteaKey
import net.rsprot.protocol.loginprot.incoming.util.LoginBlockDecodingFunction
import net.rsprot.protocol.message.IncomingLoginMessage

public typealias ReconnectDecodingFunction = LoginBlockDecodingFunction<XteaKey>

public data class GameReconnect(
    public val buffer: JagByteBuf,
    public val decoder: ReconnectDecodingFunction,
) : IncomingLoginMessage
