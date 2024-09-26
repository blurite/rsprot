package net.rsprot.protocol.metrics.channel.snapshots.util

public data class PacketSnapshot(
    public val count: Long,
    public val cumulativePayloadSize: Long,
)
