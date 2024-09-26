package net.rsprot.protocol.metrics.channel.snapshots.impl

import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.metrics.channel.snapshots.ChannelTrafficSnapshot
import net.rsprot.protocol.metrics.channel.snapshots.InetAddressSnapshot
import java.net.InetAddress
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

public class GenericChannelTrafficSnapshot<CP, SP, DC>(
    public val startDateTime: LocalDateTime,
    public val endDateTime: LocalDateTime,
    public val activeConnectionsByAddress: Map<InetAddress, Int>,
    public val totalActiveConnections: Int,
    public val inetAddressSnapshots: Map<InetAddress, InetAddressSnapshot<CP, SP, DC>>,
) : ChannelTrafficSnapshot where CP : ClientProt, CP : Enum<CP>, SP : ServerProt, SP : Enum<SP>, DC : Enum<DC> {
    public val elapsedMillis: Long
        get() = ChronoUnit.MILLIS.between(startDateTime, endDateTime)
    public val elapsed: Duration
        get() = elapsedMillis.milliseconds

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GenericChannelTrafficSnapshot<*, *, *>

        if (startDateTime != other.startDateTime) return false
        if (endDateTime != other.endDateTime) return false
        if (activeConnectionsByAddress != other.activeConnectionsByAddress) return false
        if (totalActiveConnections != other.totalActiveConnections) return false
        if (inetAddressSnapshots != other.inetAddressSnapshots) return false

        return true
    }

    override fun hashCode(): Int {
        var result = startDateTime.hashCode()
        result = 31 * result + endDateTime.hashCode()
        result = 31 * result + activeConnectionsByAddress.hashCode()
        result = 31 * result + totalActiveConnections
        result = 31 * result + inetAddressSnapshots.hashCode()
        return result
    }

    override fun toString(): String =
        "GenericChannelTrafficSnapshot(" +
            "startDateTime=$startDateTime, " +
            "endDateTime=$endDateTime, " +
            "activeConnectionsByAddress=$activeConnectionsByAddress, " +
            "totalActiveConnections=$totalActiveConnections, " +
            "inetAddressSnapshots=$inetAddressSnapshots" +
            ")"
}
