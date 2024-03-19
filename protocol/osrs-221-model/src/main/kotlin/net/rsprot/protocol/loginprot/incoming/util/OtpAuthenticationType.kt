package net.rsprot.protocol.loginprot.incoming.util

public sealed interface OtpAuthenticationType {
    public data class TrustedComputer(public val identifier: Int) : OtpAuthenticationType

    public data class TrustedAuthenticator(override val otp: Int) : OtpAuthentication

    public data object NoMultiFactorAuthentication : OtpAuthenticationType

    public data class UntrustedAuthentication(override val otp: Int) : OtpAuthentication

    public sealed interface OtpAuthentication : OtpAuthenticationType {
        /**
         * One-time password, typically referred to as authentication code.
         * This is the 6-digit 24-bit code used by two-factor authenticators.
         */
        public val otp: Int
    }
}
