package net.rsprot.protocol.loginprot.incoming.util

public sealed interface AuthenticationType<T : OtpAuthenticationType> {
    public val otpAuthentication: T

    public data class PasswordAuthentication<T : OtpAuthenticationType>(
        public val password: String,
        override val otpAuthentication: T,
    ) : AuthenticationType<T>

    public data class TokenAuthentication<T : OtpAuthenticationType>(
        public val token: String,
        override val otpAuthentication: T,
    ) : AuthenticationType<T>
}
