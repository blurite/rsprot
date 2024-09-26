package net.rsprot.protocol.metrics.channel.impl

import net.rsprot.protocol.metrics.channel.ChannelTrafficHandler
import net.rsprot.protocol.metrics.channel.snapshots.ChannelTrafficSnapshot
import net.rsprot.protocol.metrics.channel.snapshots.impl.NoopChannelTrafficSnapshot
import java.net.InetAddress
import java.time.LocalDateTime
import kotlin.time.Duration

public data object NoopChannelTrafficHandler : ChannelTrafficHandler {
    override fun incrementConnections(inetAddress: InetAddress) {
    }

    override fun decrementConnections(inetAddress: InetAddress) {
    }

    override fun addDisconnectionReason(
        inetAddress: InetAddress,
        reason: Int,
    ) {
    }

    override fun incrementIncomingPackets(
        inetAddress: InetAddress,
        opcode: Int,
        payloadSize: Int,
    ) {
    }

    override fun incrementOutgoingPackets(
        inetAddress: InetAddress,
        opcode: Int,
        payloadSize: Int,
    ) {
    }

    override fun incrementOutgoingPacketOpcode(
        inetAddress: InetAddress,
        opcode: Int,
    ) {
    }

    override fun incrementOutgoingPacketPayload(
        inetAddress: InetAddress,
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
