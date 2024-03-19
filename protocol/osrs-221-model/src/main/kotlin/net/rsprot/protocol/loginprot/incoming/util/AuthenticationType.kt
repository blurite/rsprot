package net.rsprot.protocol.loginprot.incoming.util

public sealed interface AuthenticationType<T : OtpAuthenticationType> {
    public val otpAuthentication: T

    /**
     * Clears all data stored in this [AuthenticationType].
     * OTP codes will all be set to [Int.MIN_VALUE],
     * password and token fields will be filled with zeros.
     */
    public fun clear()

    public data class PasswordAuthentication<T : OtpAuthenticationType>(
        public val password: Password,
        override val otpAuthentication: T,
    ) : AuthenticationType<T> {
        override fun clear() {
            otpAuthentication.clear()
            password.clear()
        }
    }

    public data class TokenAuthentication<T : OtpAuthenticationType>(
        public val token: Token,
        override val otpAuthentication: T,
    ) : AuthenticationType<T> {
        override fun clear() {
            otpAuthentication.clear()
            token.clear()
        }
    }
}
