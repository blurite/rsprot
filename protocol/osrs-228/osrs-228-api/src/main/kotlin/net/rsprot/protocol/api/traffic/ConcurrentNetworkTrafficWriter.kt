package net.rsprot.protocol.api.traffic

import net.rsprot.protocol.loginprot.incoming.util.HostPlatformStats
import net.rsprot.protocol.loginprot.incoming.util.LoginBlock
import net.rsprot.protocol.metrics.channel.snapshots.impl.ConcurrentChannelTrafficSnapshot
import net.rsprot.protocol.metrics.channel.snapshots.util.PacketSnapshot
import net.rsprot.protocol.metrics.snapshots.impl.ConcurrentNetworkTrafficSnapshot
import net.rsprot.protocol.metrics.writer.NetworkTrafficWriter
import java.net.InetAddress
import java.text.NumberFormat
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlin.collections.iterator

public data object ConcurrentNetworkTrafficWriter : NetworkTrafficWriter<ConcurrentNetworkTrafficSnapshot<*>, String> {
    private const val INDENT: String = "  "
    private val dateTimeFormatter =
        DateTimeFormatter
            .ofLocalizedDateTime(FormatStyle.FULL)
            .withZone(ZoneId.systemDefault())

    override fun write(snapshot: ConcurrentNetworkTrafficSnapshot<*>): String =
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
                snapshot.loginSnapshot as ConcurrentChannelTrafficSnapshot<*, *, *>,
                "Login",
            )
            appendLine()
            appendChannelMetrics(
                snapshot.js5Snapshot as ConcurrentChannelTrafficSnapshot<*, *, *>,
                "JS5",
            )
            appendLine()
            appendChannelMetrics(
                snapshot.gameSnapshot as ConcurrentChannelTrafficSnapshot<*, *, *>,
                "Game",
            )
            appendLine()
            @Suppress("UNCHECKED_CAST")
            val loginBlocks = snapshot.loginBlocks as Map<InetAddress, List<LoginBlock<*>>>
            appendLoginBlocks(loginBlocks)
        }

    private fun StringBuilder.appendLoginBlocks(loginBlocks: Map<InetAddress, List<LoginBlock<*>>>) {
        appendLine("Login Blocks")
        for ((k, v) in loginBlocks) {
            indent().append("Inet Address: ").appendLine(k)
            for (block in v) {
                indent(2).appendLine("Login Block")
                indent(3).append("Version: ").appendLine(block.version)
                indent(3).append("Sub Version: ").appendLine(block.subVersion)
                indent(3).append("Client Type: ").appendLine(block.clientType)
                indent(3).append("Platform Type: ").appendLine(block.platformType)
                indent(3).append("Seed: ").appendLine(block.seed.contentToString())
                indent(3).append("Session Id: ").appendLine(format(block.sessionId))
                indent(3).append("Username: ").appendLine(block.username)
                indent(3).append("Low Detail: ").appendLine(block.lowDetail)
                indent(3).append("Resizable: ").appendLine(block.resizable)
                indent(3).append("Width: ").appendLine(block.width)
                indent(3).append("Height: ").appendLine(block.height)
                indent(3).append("UUID: ").appendLine(block.uuid.contentToString())
                indent(3).append("Site Settings: ").appendLine(block.siteSettings)
                indent(3).append("Affiliate: ").appendLine(block.affiliate)
                indent(3).append("CRC Header: ").appendLine(block.crcBlockHeader)
                indent(3).append("CRC: ").appendLine(block.crc.toIntArray().contentToString())
                appendHostPlatformStats(block.hostPlatformStats)
            }
        }
    }

    private fun StringBuilder.appendHostPlatformStats(stats: HostPlatformStats) {
        indent(3).appendLine("Host Platform Stats")
        indent(4).append("Version: ").appendLine(stats.version)
        indent(4).append("OS Type: ").appendLine(stats.osType)
        indent(4).append("OS 64 Bit: ").appendLine(stats.os64Bit)
        indent(4).append("OS Version: ").appendLine(stats.osVersion)
        indent(4).append("Java Vendor: ").appendLine(stats.javaVendor)
        indent(4)
            .append("Java: ")
            .append(stats.javaVendor)
            .append(" ")
            .append(stats.javaVersionMajor)
            .append(".")
            .append(stats.javaVersionMinor)
            .append(".")
            .appendLine(stats.javaVersionPatch)

        indent(4).append("Applet: ").appendLine(stats.applet)
        indent(4).append("Java Max Memory (MB): ").appendLine(stats.javaMaxMemoryMb)
        indent(4).append("Java Available Processors: ").appendLine(stats.javaAvailableProcessors)
        indent(4).append("System Memory: ").appendLine(stats.systemMemory)
        indent(4).append("System Speed: ").appendLine(stats.systemSpeed)
        indent(4).append("GPU DX Name: ").appendLine(stats.gpuDxName)
        indent(4).append("GPU GL Name: ").appendLine(stats.gpuGlName)
        indent(4).append("GPU GL Version: ").appendLine(stats.gpuGlVersion)
        indent(4)
            .append("GPU Driver Date: ")
            .append(stats.gpuDriverMonth)
            .append(".")
            .appendLine(stats.gpuDriverYear)
        indent(4).append("CPU Manufacturer: ").appendLine(stats.cpuManufacturer)
        indent(4).append("CPU Brand: ").appendLine(stats.cpuBrand)
        indent(4)
            .append("CPU Counts: ")
            .append(stats.cpuCount1)
            .append(", ")
            .appendLine(stats.cpuCount2)
        indent(4).append("CPU Features: ").appendLine(stats.cpuFeatures.contentToString())
        indent(4).append("CPU Signature: ").appendLine(stats.cpuSignature)
        indent(4).append("Client Name: ").appendLine(stats.clientName)
        indent(4).append("Device Name: ").appendLine(stats.deviceName)
    }

    private fun StringBuilder.appendChannelMetrics(
        snapshot: ConcurrentChannelTrafficSnapshot<*, *, *>,
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
        channelSnapshot: ConcurrentChannelTrafficSnapshot<*, *, *>,
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
                .append(format(v.cumulativePayloadSize))
                .appendLine(if (v.cumulativePayloadSize == 1L) " byte" else " bytes")
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
