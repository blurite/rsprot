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

    override fun hashCode(): Int {
        return crc.contentHashCode()
    }

    override fun toString(): String {
        return "RemainingBetaArchives(crc=${crc.contentToString()})"
    }
}
