package net.rsprot.protocol.api

import net.rsprot.crypto.util.XteaKey
import net.rsprot.protocol.api.login.GameLoginResponseHandler
import net.rsprot.protocol.loginprot.incoming.util.AuthenticationType
import net.rsprot.protocol.loginprot.incoming.util.LoginBlock

public interface GameLoginHandler {
    public fun onLogin(
        responseHandler: GameLoginResponseHandler,
        block: LoginBlock<AuthenticationType<*>>,
    )

    public fun onReconnect(
        responseHandler: GameLoginResponseHandler,
        block: LoginBlock<XteaKey>,
    )
}
