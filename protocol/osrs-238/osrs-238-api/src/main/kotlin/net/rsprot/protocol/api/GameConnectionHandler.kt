package net.rsprot.protocol.api

import net.rsprot.crypto.xtea.XteaKey
import net.rsprot.protocol.api.login.GameLoginResponseHandler
import net.rsprot.protocol.loginprot.incoming.util.AuthenticationType
import net.rsprot.protocol.loginprot.incoming.util.LoginBlock

/**
 * A handler interface for any game logins and reconnections.
 * @param R the receiver of the incoming game packets, typically a Player class.
 */
public interface GameConnectionHandler<R> {
    /**
     * The onLogin function is triggered whenever a login request is received by the library,
     * and it passes all the initial validation necessary. The server is responsible
     * for doing most of the validation here, but preliminary things like max number of connections
     * and session ids will have been pre-checked by us.
     * @param responseHandler the handler used to write a successful or failed login response,
     * depending on the decisions made by the server.
     * @param block the login block sent by the client, containing all the information the server
     * will need.
     */
    public fun onLogin(
        responseHandler: GameLoginResponseHandler<R>,
        block: LoginBlock<AuthenticationType>,
    )

    /**
     * The onReconnect function is triggered whenever a reconnect request is received
     * by the library. It is worth noting that Proof of Work will not be involved
     * if this is the case, assuming it is enabled in the first place.
     * Instead of transmitting the password, the client will transmit the seed used
     * by the previous login connection. If the seed does not match with what the
     * server knows, the request should be rejected. If the reconnect is successful,
     * the server should replace the Session object in that player with the one
     * provided by the response handler. The old session will close or time out shortly
     * afterwards, if it already hasn't.
     * @param responseHandler the handler used to write a successful or failed reconnect response,
     * depending on the decisions made by the server.
     * @param block the login block sent by the client, containing all the information the server
     * will need.
     */
    public fun onReconnect(
        responseHandler: GameLoginResponseHandler<R>,
        block: LoginBlock<XteaKey>,
    )
}
