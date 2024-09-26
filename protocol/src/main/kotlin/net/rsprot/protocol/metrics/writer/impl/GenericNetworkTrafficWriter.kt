package net.rsprot.protocol.metrics.writer.impl

import net.rsprot.protocol.metrics.channel.snapshots.impl.GenericChannelTrafficSnapshot
import net.rsprot.protocol.metrics.channel.snapshots.util.PacketSnapshot
import net.rsprot.protocol.metrics.snapshots.impl.GenericNetworkTrafficSnapshot
import net.rsprot.protocol.metrics.writer.NetworkTrafficWriter
import java.text.NumberFormat
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlin.collections.Map

public data object GenericNetworkTrafficWriter : NetworkTrafficWriter<GenericNetworkTrafficSnapshot, String> {
    private const val INDENT: String = "  "
    private val dateTimeFormatter =
        DateTimeFormatter
            .ofLocalizedDateTime(FormatStyle.FULL)
            .withZone(ZoneId.systemDefault())

    override fun write(snapshot: GenericNetworkTrafficSnapshot): String =
        buildString {
            val start = dateTimeFormatter.format(snapshot.startDateTime)
            val end = dateTimeFormatter.format(snapshot.endDateTime)
            appendLine("Network Traffic Snapshot")
            indent().append("Snapshot started on: ").appendLine(start)
            indent().append("Snapshot ended on: ").appendLine(end)
            indent().append("Elapsed duration: ").appendLine(snapshot.elapsed)
            indent()
                .append("Connection requests: ")
                .appendLine(format(snapshot.connectionRequests))
            appendLine()
            appendChannelMetrics(
                snapshot.loginSnapshot as GenericChannelTrafficSnapshot<*, *, *>,
                "Login",
            )
            appendLine()
            appendChannelMetrics(
                snapshot.js5Snapshot as GenericChannelTrafficSnapshot<*, *, *>,
                "JS5",
            )
            appendLine()
            appendChannelMetrics(
                snapshot.gameSnapshot as GenericChannelTrafficSnapshot<*, *, *>,
                "Game",
            )
        }

    private fun StringBuilder.appendChannelMetrics(
        snapshot: GenericChannelTrafficSnapshot<*, *, *>,
        title: String,
    ) {
        append(title).appendLine(" Channel Snapshot")
        val start = dateTimeFormatter.format(snapshot.startDateTime)
        val end = dateTimeFormatter.format(snapshot.endDateTime)
        indent().append(title).append(" snapshot started on: ").appendLine(start)
        indent().append(title).append(" snapshot ended on: ").appendLine(end)
        indent().append(title).append(" elapsed duration: ").appendLine(snapshot.elapsed)
        appendInetAddressMetrics(snapshot, title)
    }

    private fun StringBuilder.appendInetAddressMetrics(
        channelSnapshot: GenericChannelTrafficSnapshot<*, *, *>,
        title: String,
    ) {
        indent().append(title).appendLine(" INet Address Metrics")
        val untrackedConnections =
            channelSnapshot
                .activeConnectionsByAddress
                .entries
                .filter {
                    it.key !in channelSnapshot.inetAddressSnapshots
                }
        for ((address, count) in untrackedConnections) {
            indent(2).append("INet Address: ").appendLine(address)
            indent(3).append("Active connections: ").appendLine(count)
        }
        for ((address, ss) in channelSnapshot.inetAddressSnapshots) {
            indent(2).append("INet Address: ").appendLine(address)
            val activeConnections = channelSnapshot.activeConnectionsByAddress[address]
            if (activeConnections != null) {
                indent(3).append("Active connections: ").appendLine(activeConnections)
            }
            appendPacketSnapshots("Incoming", ss.incomingPackets)
            appendPacketSnapshots("Outgoing", ss.outgoingPackets)
            appendDisconnectionReasons(ss.disconnectionsByReason)
        }
    }

    private fun StringBuilder.appendPacketSnapshots(
        prefix: String,
        map: Map<*, PacketSnapshot>,
    ) {
        val incoming =
            map
                .filterNot { it.value.count == 0L }
                .entries
                .sortedWith(
                    compareBy<Map.Entry<Any?, PacketSnapshot>>(
                        { it.value.cumulativePayloadSize },
                        { it.value.count },
                    ).reversed(),
                )
        for ((k, v) in incoming) {
            indent(3)
                .append(prefix)
                .append(" packet: ")
                .append(k)
                .append(", count: ")
                .append(format(v.count))
                .append(", payload sum: ")
                .appendLine(format(v.cumulativePayloadSize))
        }
    }

    private fun StringBuilder.appendDisconnectionReasons(map: Map<*, Int>) {
        val incoming =
            map
                .filterNot { it.value == 0 }
                .entries
                .sortedByDescending { it.value }
        for ((k, v) in incoming) {
            indent(3)
                .append("Disconnection reason: ")
                .append(k)
                .append(", count: ")
                .appendLine(format(v))
        }
    }

    private fun format(number: Int): String = NumberFormat.getIntegerInstance().format(number)

    private fun format(number: Long): String = NumberFormat.getIntegerInstance().format(number)

    private fun StringBuilder.indent(count: Int = 1): StringBuilder =
        apply {
            repeat(count) {
                append(INDENT)
            }
        }
}
