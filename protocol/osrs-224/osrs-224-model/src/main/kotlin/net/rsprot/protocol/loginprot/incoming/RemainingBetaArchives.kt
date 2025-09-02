package net.rsprot.protocol.loginprot.incoming

import net.rsprot.protocol.message.IncomingLoginMessage

public class RemainingBetaArchives(
    internal val crc: IntArray,
) : IncomingLoginMessage {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RemainingBetaArchives

        return crc.contentEquals(other.crc)
    }

    override fun hashCode(): Int = crc.contentHashCode()

    override fun toString(): String = "RemainingBetaArchives(crc=${crc.contentToString()})"

    public companion object {
        public val protectedArchives: List<Int> =
            listOf(
                0,
                1,
                2,
                3,
                5,
                7,
                9,
                11,
                12,
                16,
                17,
                18,
                19,
                20,
            )
    }
}
