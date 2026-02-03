package net.rsprot.protocol.loginprot.incoming.util

public sealed interface AuthenticationType {
    public val otpAuthentication: OtpAuthenticationType

    /**
     * Clears all data stored in this [AuthenticationType].
     * OTP codes will all be set to [Int.MIN_VALUE],
     * password and token fields will be filled with zeros.
     */
    public fun clear()

    public data class PasswordAuthentication(
        public val password: Password,
        override val otpAuthentication: OtpAuthenticationType,
    ) : AuthenticationType {
        override fun clear() {
            otpAuthentication.clear()
            password.clear()
        }
    }

    public data class TokenAuthentication(
        public val token: Token,
        override val otpAuthentication: OtpAuthenticationType,
    ) : AuthenticationType {
        override fun clear() {
            otpAuthentication.clear()
            token.clear()
        }
    }
}
