package net.rsprot.protocol.loginprot.incoming

import net.rsprot.protocol.message.IncomingLoginMessage

public class InitJs5RemoteConnection(
    public val revision: Int,
    public val seed: IntArray,
) : IncomingLoginMessage {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as InitJs5RemoteConnection

        if (revision != other.revision) return false
        if (!seed.contentEquals(other.seed)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = revision
        result = 31 * result + seed.contentHashCode()
        return result
    }

    override fun toString(): String =
        "InitJs5RemoteConnection(" +
            "revision=$revision, " +
            "seed=${seed.contentToString()}" +
            ")"
}
