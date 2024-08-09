package net.rsprot.protocol.loginprot.incoming.util

public sealed interface OtpAuthenticationType {
    /**
     * Clear any data stored in the one-time passwords by setting the
     * payload to [Int.MIN_VALUE], if there is a payload attached.
     */
    public fun clear()

    public data class TrustedComputer(
        public var identifier: Int,
    ) : OtpAuthenticationType {
        override fun clear() {
            identifier = Int.MIN_VALUE
        }
    }

    public data class TrustedAuthenticator(
        override var otp: Int,
    ) : OtpAuthentication {
        override fun clear() {
            otp = Int.MIN_VALUE
        }
    }

    public data object NoMultiFactorAuthentication : OtpAuthenticationType {
        override fun clear() {
        }
    }

    public data class UntrustedAuthentication(
        override var otp: Int,
    ) : OtpAuthentication {
        override fun clear() {
            otp = Int.MIN_VALUE
        }
    }

    public sealed interface OtpAuthentication : OtpAuthenticationType {
        /**
         * One-time password, typically referred to as authentication code.
         * This is the 6-digit 24-bit code used by two-factor authenticators.
         */
        public var otp: Int
    }
}
