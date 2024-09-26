package net.rsprot.protocol.loginprot.incoming.util

/**
 * CRC blocks are helper structures used for the server to verify that the CRC is up-to-date.
 * As the client transmits less CRCs than there are cache indices, we provide validation methods
 * through this abstract class at the respective revision's decoder level, so we can perform checks
 * that correspond to the information received from the client, and not what the server fully knows of.
 * @property clientCrc the int array of client CRCs, indexed by the cache archives.
 */
public abstract class CyclicRedundancyCheckBlock(
    protected val clientCrc: IntArray,
) {
    public abstract fun validate(serverCrc: IntArray): Boolean

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CyclicRedundancyCheckBlock) return false

        if (!clientCrc.contentEquals(other.clientCrc)) return false

        return true
    }

    internal fun set(
        index: Int,
        value: Int,
    ) {
        this.clientCrc[index] = value
    }

    public fun toIntArray(): IntArray = clientCrc.copyOf()

    override fun hashCode(): Int = clientCrc.contentHashCode()

    override fun toString(): String = "CyclicRedundancyCheckBlock(clientCrc=${clientCrc.contentToString()})"
}
