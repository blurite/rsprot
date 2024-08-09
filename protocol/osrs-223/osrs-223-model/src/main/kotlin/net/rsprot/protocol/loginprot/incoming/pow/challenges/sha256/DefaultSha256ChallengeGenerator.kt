package net.rsprot.protocol.loginprot.incoming.pow.challenges.sha256

import net.rsprot.protocol.loginprot.incoming.pow.challenges.ChallengeGenerator
import java.math.BigInteger
import kotlin.random.Random

/**
 * The default SHA-256 challenge generator is used to generate challenges which align
 * up with what OldSchool RuneScape is generating, which is a combination of epoch time millis,
 * the world id and a 495-byte [BigInteger] that is turned into a hexadecimal string,
 * which will have a length of 1004 or 1005 characters, depending on if the [BigInteger] was negative.
 */
public class DefaultSha256ChallengeGenerator : ChallengeGenerator<Sha256MetaData, Sha256Challenge> {
    override fun generate(input: Sha256MetaData): Sha256Challenge {
        val randomData = Random.Default.nextBytes(RANDOM_DATA_LENGTH)
        val hexSalt = BigInteger(randomData).toString(HEX_RADIX)
        val salt =
            java.lang.Long.toHexString(input.epochTimeMillis) +
                Integer.toHexString(input.world) +
                hexSalt
        return Sha256Challenge(
            input.unknown,
            input.difficulty,
            salt,
        )
    }

    private companion object {
        private const val RANDOM_DATA_LENGTH: Int = 495
        private const val HEX_RADIX: Int = 16
    }
}
