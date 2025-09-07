package net.rsprot.protocol.metrics.channel.impl

import net.rsprot.protocol.metrics.channel.ChannelTrafficMonitor
import net.rsprot.protocol.metrics.channel.snapshots.ChannelTrafficSnapshot
import net.rsprot.protocol.metrics.channel.snapshots.impl.NoopChannelTrafficSnapshot
import java.time.LocalDateTime
import kotlin.time.Duration

/**
 * A no-op channel traffic monitor. All function calls here do nothing.
 */
public data object NoopChannelTrafficMonitor : ChannelTrafficMonitor {
    override fun incrementConnections(hostAddress: String) {
    }

    override fun decrementConnections(hostAddress: String) {
    }

    override fun addDisconnectionReason(
        hostAddress: String,
        reason: Int,
    ) {
    }

    override fun incrementIncomingPackets(
        hostAddress: String,
        opcode: Int,
        payloadSize: Int,
    ) {
    }

    override fun incrementOutgoingPackets(
        hostAddress: String,
        opcode: Int,
        payloadSize: Int,
    ) {
    }

    override fun incrementOutgoingPacketOpcode(
        hostAddress: String,
        opcode: Int,
    ) {
    }

    override fun incrementOutgoingPacketPayload(
        hostAddress: String,
        opcode: Int,
        payloadSize: Int,
    ) {
    }

    override fun startDateTime(): LocalDateTime = LocalDateTime.MIN

    override fun elapsed(): Duration = Duration.ZERO

    override fun elapsedMillis(): Long = elapsed().inWholeMilliseconds

    override fun snapshot(): ChannelTrafficSnapshot = NoopChannelTrafficSnapshot

    override fun resetTransient(): ChannelTrafficSnapshot = NoopChannelTrafficSnapshot

    override fun freeze() {
    }

    override fun unfreeze() {
    }
}
