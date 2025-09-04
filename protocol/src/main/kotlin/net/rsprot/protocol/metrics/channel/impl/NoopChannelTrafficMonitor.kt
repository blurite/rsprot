package net.rsprot.protocol.metrics.channel.impl

import net.rsprot.protocol.metrics.channel.ChannelTrafficMonitor
import net.rsprot.protocol.metrics.channel.snapshots.ChannelTrafficSnapshot
import net.rsprot.protocol.metrics.channel.snapshots.impl.NoopChannelTrafficSnapshot
import java.net.SocketAddress
import java.time.LocalDateTime
import kotlin.time.Duration

/**
 * A no-op channel traffic monitor. All function calls here do nothing.
 */
public data object NoopChannelTrafficMonitor : ChannelTrafficMonitor {
    override fun incrementConnections(socketAddress: SocketAddress) {
    }

    override fun decrementConnections(socketAddress: SocketAddress) {
    }

    override fun addDisconnectionReason(
        socketAddress: SocketAddress,
        reason: Int,
    ) {
    }

    override fun incrementIncomingPackets(
        socketAddress: SocketAddress,
        opcode: Int,
        payloadSize: Int,
    ) {
    }

    override fun incrementOutgoingPackets(
        socketAddress: SocketAddress,
        opcode: Int,
        payloadSize: Int,
    ) {
    }

    override fun incrementOutgoingPacketOpcode(
        socketAddress: SocketAddress,
        opcode: Int,
    ) {
    }

    override fun incrementOutgoingPacketPayload(
        socketAddress: SocketAddress,
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
