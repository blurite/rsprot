package net.rsprot.crypto.rsa

import java.math.BigInteger

public class RsaKeyPair(
    public val exponent: BigInteger,
    public val modulus: BigInteger,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RsaKeyPair

        if (exponent != other.exponent) return false
        if (modulus != other.modulus) return false

        return true
    }

    override fun hashCode(): Int {
        var result = exponent.hashCode()
        result = 31 * result + modulus.hashCode()
        return result
    }

    override fun toString(): String {
        return "RsaKeyPair(" +
            "exponent=$exponent, " +
            "modulus=$modulus" +
            ")"
    }
}
