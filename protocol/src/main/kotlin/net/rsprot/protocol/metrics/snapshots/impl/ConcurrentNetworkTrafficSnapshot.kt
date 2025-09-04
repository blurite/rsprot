package net.rsprot.protocol.metrics.snapshots.impl

import net.rsprot.protocol.metrics.channel.snapshots.ChannelTrafficSnapshot
import net.rsprot.protocol.metrics.snapshots.NetworkTrafficSnapshot
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * The concurrent network traffic snapshot is a result of calling the
 * [net.rsprot.protocol.metrics.impl.ConcurrentNetworkTrafficMonitor.snapshot] function.
 * This data structure tracks any network traffic that occurred during a specific time period.
 * Note that the [net.rsprot.protocol.metrics.NetworkTrafficMonitor.freeze] is ignored when
 * it comes to the snapshots, no information is provided regarding the freeze periods.
 *
 * @property startDateTime the local datetime when the tracking began.
 * @property endDateTime the local datetime when the snapshot was captured.
 * @property connectionRequests the number of connection requests that were received.
 * @property loginBlocks the complete login blocks that were received from each [String],
 * in the order that they were received.
 * @property loginSnapshot a snapshot of the login channel's traffic.
 * @property js5Snapshot a snapshot of the JS5 channel's traffic.
 * @property gameSnapshot a snapshot of the game channel's traffic.
 * @property elapsedMillis the number of milliseconds that this snapshot covers.
 * @property elapsed the duration that this snapshot covers.
 */
public class ConcurrentNetworkTrafficSnapshot<LoginBlock>(
    public val startDateTime: LocalDateTime,
    public val endDateTime: LocalDateTime,
    public val connectionRequests: Int,
    public val loginBlocks: Map<String, List<LoginBlock>>,
    public val loginSnapshot: ChannelTrafficSnapshot,
    public val js5Snapshot: ChannelTrafficSnapshot,
    public val gameSnapshot: ChannelTrafficSnapshot,
) : NetworkTrafficSnapshot {
    public val elapsedMillis: Long
        get() = ChronoUnit.MILLIS.between(startDateTime, endDateTime)
    public val elapsed: Duration
        get() = elapsedMillis.milliseconds

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ConcurrentNetworkTrafficSnapshot<*>

        if (startDateTime != other.startDateTime) return false
        if (endDateTime != other.endDateTime) return false
        if (connectionRequests != other.connectionRequests) return false
        if (loginBlocks != other.loginBlocks) return false
        if (loginSnapshot != other.loginSnapshot) return false
        if (js5Snapshot != other.js5Snapshot) return false
        if (gameSnapshot != other.gameSnapshot) return false

        return true
    }

    override fun hashCode(): Int {
        var result = startDateTime.hashCode()
        result = 31 * result + endDateTime.hashCode()
        result = 31 * result + connectionRequests
        result = 31 * result + loginBlocks.hashCode()
        result = 31 * result + loginSnapshot.hashCode()
        result = 31 * result + js5Snapshot.hashCode()
        result = 31 * result + gameSnapshot.hashCode()
        return result
    }

    override fun toString(): String =
        "ConcurrentNetworkTrafficSnapshot(" +
            "startDateTime=$startDateTime, " +
            "endDateTime=$endDateTime, " +
            "connectionRequests=$connectionRequests, " +
            "loginBlocks=$loginBlocks, " +
            "loginSnapshot=$loginSnapshot, " +
            "js5Snapshot=$js5Snapshot, " +
            "gameSnapshot=$gameSnapshot, " +
            "elapsedMillis=$elapsedMillis, " +
            "elapsed=$elapsed" +
            ")"
}
