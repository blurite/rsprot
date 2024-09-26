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

/**
 * A concurrent channel traffic snapshot is the result of calling
 * [net.rsprot.protocol.metrics.channel.impl.ConcurrentChannelTrafficMonitor.snapshot].
 * This snapshot will track any metrics behind a specific channel type (login, JS5, game),
 * such as the number of active connections from each [InetAddress] at the time of capturing
 * the snapshot, as well as a general overview of the traffic that a specific channel witnessed.
 *
 * @property startDateTime the local datetime when the tracking began for this snapshot.
 * @property endDateTime the local datetime when the snapshot was captured.
 * @property activeConnectionsByAddress the active connections at the time of capturing the snapshot,
 * organized per [InetAddress].
 * @property totalActiveConnections the number of total active connections.
 * @property inetAddressSnapshots the snapshots per [InetAddress], containing any
 * packet traffic, disconnection reasons and more.
 * @property elapsedMillis the number of milliseconds that this snapshot covers.
 * @property elapsed the duration that this snapshot covers.
 */
public class ConcurrentChannelTrafficSnapshot<CP, SP, DC>(
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

        other as ConcurrentChannelTrafficSnapshot<*, *, *>

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
        "ConcurrentChannelTrafficSnapshot(" +
            "startDateTime=$startDateTime, " +
            "endDateTime=$endDateTime, " +
            "activeConnectionsByAddress=$activeConnectionsByAddress, " +
            "totalActiveConnections=$totalActiveConnections, " +
            "inetAddressSnapshots=$inetAddressSnapshots" +
            ")"
}
