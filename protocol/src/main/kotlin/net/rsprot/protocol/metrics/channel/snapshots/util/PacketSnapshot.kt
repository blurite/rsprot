package net.rsprot.protocol.metrics.channel.snapshots.util

/**
 * A data class to track a snapshot of a given packet.
 * @property count the number of times this packet was sent or received.
 * @property cumulativePayloadSize the number of bytes that were sent or received
 * in total from sending or receiving [count] number of this packet.
 * The payload size **does not** include the 1-2 bytes for the opcode, nor the
 * 1-2 bytes for the true payload size. It is strictly only the sum of the payloads
 * themselves, and can be zero for constant packets with no payloads.
 */
public data class PacketSnapshot(
    public val count: Long,
    public val cumulativePayloadSize: Long,
)
