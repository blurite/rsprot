package net.rsprot.protocol.loginprot.outgoing.util

public sealed interface AuthenticatorResponse {
    public data object NoAuthenticator : AuthenticatorResponse

    public data class AuthenticatorCode(public val code: Int) : AuthenticatorResponse
}
