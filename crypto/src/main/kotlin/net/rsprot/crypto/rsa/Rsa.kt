package net.rsprot.crypto.rsa

import com.github.michaelbull.logging.InlineLogger
import com.squareup.jnagmp.Gmp
import java.math.BigInteger

/**
 * A helper object to perform RSA encryption or decryption of a block of data.
 */
public object Rsa {
    /**
     * Whether the current platform supports the native JNA-GMP library.
     */
    private var jnaGmpSupport: Boolean = false
    private val logger: InlineLogger = InlineLogger()

    init {
        // If the current platform doesn't support JNA-GMP, the checkLoaded will throw an
        // UnsatisfiedLinkError or an AssertionError.
        this.jnaGmpSupport =
            try {
                Gmp.checkLoaded()
                true
            } catch (t: Throwable) {
                false
            }
        logger.debug {
            "Native JNA-GMP support: ${this.jnaGmpSupport}"
        }
    }

    /**
     * Checks whether native support is available for RSA encryption/decryption.
     */
    private fun supportsNative(): Boolean {
        return jnaGmpSupport
    }

    /**
     * Performs a modPow operation on the [base] big integer, using [exp] and [mod] for encryption/decryption.
     * @param base the base input data to be encrypted or decrypted
     * @param exp the exponent to use for encryption/decryption
     * @param mod the modulo to use. Note that even modulo are not supported with native JNA-GMP,
     * if an even modulo is provided, native support is disabled and code falls back to traditional variant.
     * @param preferNative whether to prefer native JNA-GMP library for the modPow operation or use
     * the normal big integer variant from java. The native variant is about ~4.7 times faster than
     * the java variant. Native is only supported for Mac and 64-bit Linux. For windows and other
     * variants, the traditional java variant is used.
     * @return a big integer that is the result of `base^exponent % mod`
     * @throws ArithmeticException mod ≤ 0 or the exponent is negative and base is not relatively prime to mod.
     */
    public fun modPow(
        base: BigInteger,
        exp: BigInteger,
        mod: BigInteger,
        preferNative: Boolean = true,
    ): BigInteger {
        return if (preferNative && supportsNative()) {
            try {
                Gmp.modPowSecure(base, exp, mod)
            } catch (e: IllegalArgumentException) {
                // Native library cannot support even modulo, if an even one is provided,
                // an IllegalArgumentException is thrown; if this is the case, turn off
                // native support and fall back to the traditional modPow implementation.
                // This is due to the modulo likely being the same throughout the runtime,
                // so consistently running into this problem is not ideal.
                logger.warn {
                    "Even modulo $mod provided; native RSA support is not supported for even modulo."
                }
                this.jnaGmpSupport = false
                modPow(base, exp, mod, preferNative = false)
            }
        } else {
            base.modPow(exp, mod)
        }
    }
}
