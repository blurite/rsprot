package net.rsprot.protocol.metrics.channel

import net.rsprot.protocol.metrics.channel.snapshots.ChannelTrafficSnapshot
import java.net.InetAddress
import java.time.LocalDateTime
import kotlin.time.Duration

public interface ChannelTrafficHandler {
    public fun incrementConnections(inetAddress: InetAddress)

    public fun decrementConnections(inetAddress: InetAddress)

    public fun addDisconnectionReason(
        inetAddress: InetAddress,
        reason: Int,
    )

    public fun incrementIncomingPackets(
        inetAddress: InetAddress,
        opcode: Int,
        payloadSize: Int,
    )

    public fun incrementOutgoingPackets(
        inetAddress: InetAddress,
        opcode: Int,
        payloadSize: Int,
    )

    public fun incrementOutgoingPacketOpcode(
        inetAddress: InetAddress,
        opcode: Int,
    )

    public fun incrementOutgoingPacketPayload(
        inetAddress: InetAddress,
        opcode: Int,
        payloadSize: Int,
    )

    public fun startDateTime(): LocalDateTime

    @JvmSynthetic
    public fun elapsed(): Duration

    public fun elapsedMillis(): Long

    public fun snapshot(): ChannelTrafficSnapshot

    public fun resetTransient(): ChannelTrafficSnapshot
}
