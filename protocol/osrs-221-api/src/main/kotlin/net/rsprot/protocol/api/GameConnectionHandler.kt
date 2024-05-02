package net.rsprot.protocol.api

import net.rsprot.crypto.xtea.XteaKey
import net.rsprot.protocol.api.login.GameLoginResponseHandler
import net.rsprot.protocol.loginprot.incoming.util.AuthenticationType
import net.rsprot.protocol.loginprot.incoming.util.LoginBlock

public interface GameConnectionHandler<R> {
    public fun onLogin(
        responseHandler: GameLoginResponseHandler<R>,
        block: LoginBlock<AuthenticationType<*>>,
    )

    public fun onReconnect(
        responseHandler: GameLoginResponseHandler<R>,
        block: LoginBlock<XteaKey>,
    )
}
