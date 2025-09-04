package net.rsprot.protocol.metrics.channel

import net.rsprot.protocol.metrics.channel.snapshots.ChannelTrafficSnapshot
import java.time.LocalDateTime
import kotlin.time.Duration

/**
 * The channel traffic monitor is used to track various metrics related to Netty,
 * such as connection requests, active connections, packet traffic, disconnection
 * reasons and more.
 * Any implementation of this interface should first and foremost be performant,
 * since most calls to it will be done off of Netty's threads which must not be
 * blocked.
 *
 * Currently, two implementations of this interface are provided as of Revision 225 onwards,
 * the [net.rsprot.protocol.metrics.channel.impl.ConcurrentChannelTrafficMonitor] and the
 * [net.rsprot.protocol.metrics.channel.impl.NoopChannelTrafficMonitor], the latter of
 * which is the default.
 */
public interface ChannelTrafficMonitor {
    /**
     * Increments connections from the provided [String]. This function is called
     * when a channel handler is registered in Netty.
     * @param inetAddress the address which is establishing a connection.
     */
    public fun incrementConnections(hostAddress: String)

    /**
     * Decrements connections from the provided [String]. This function is called
     * when a channel handler is _unregistered_ in Netty. Whenever a handler
     * switches from one to another, the old one will be unregistered first, before
     * the new one is registered. An example of this is moving from login to JS5.
     * The exact flow is:
     * 1. Register login
     * 2. Unregister login
     * 3. Register JS5
     * 4. Unregister JS5
     *
     * @param inetAddress the address to decrement a connection from.
     */
    public fun decrementConnections(hostAddress: String)

    /**
     * Adds a channel disconnection reason from the provided [String].
     * @param inetAddress the address which was disconnected.
     * @param reason the numeric reason behind the disconnection. These values
     * are backed by an enum provided in the corresponding
     * [net.rsprot.protocol.metrics.NetworkTrafficMonitor], from which the
     * [Enum.ordinal] value is used for the numeric representation.
     */
    public fun addDisconnectionReason(
        hostAddress: String,
        reason: Int,
    )

    /**
     * Increments the incoming packets received from the provided [String].
     * @param inetAddress the address from which a packet was received.
     * @param opcode the opcode of the packet which was received. The opcodes
     * correspond to the ones found in the client.
     * @param payloadSize the number of bytes that the payload is made out of.
     * It should be noted that the payload size _only_ includes the payload, and
     * not the 1-2 bytes for the opcode, nor the 1-2 bytes for the value defining
     * the payload size. Both of those values can be retroactively determined,
     * if more accurate metrics are required. In any case, however, due to the TCP
     * protocol having a significant amount of overhead for its packets on-top of it,
     * the metrics would never be truly accurate in terms of the data received.
     */
    public fun incrementIncomingPackets(
        hostAddress: String,
        opcode: Int,
        payloadSize: Int,
    )

    /**
     * Increments the outgoing packets sent to the provided [String].
     * @param inetAddress the address to which a packet was sent.
     * @param opcode the opcode of the packet which was sent. The opcodes
     * correspond to the ones found in the client.
     * @param payloadSize the number of bytes that the payload is made out of.
     * It should be noted that the payload size _only_ includes the payload, and
     * not the 1-2 bytes for the opcode, nor the 1-2 bytes for the value defining
     * the payload size. Both of those values can be retroactively determined,
     * if more accurate metrics are required. In any case, however, due to the TCP
     * protocol having a significant amount of overhead for its packets on-top of it,
     * the metrics would never be truly accurate in terms of the data received.
     */
    public fun incrementOutgoingPackets(
        hostAddress: String,
        opcode: Int,
        payloadSize: Int,
    )

    /**
     * Increments the outgoing packet opcode counter alone, without the payload increase.
     * We require a special function for this since our JS5Service implementation fragments
     * the payloads into blocks of X bytes. Due to the nature of it, we may only have a
     * single packet header, but a hundred slices of that packet's payload.
     * @param inetAddress the address to which we are sending a packet.
     * @param opcode the opcode of the packet which was sent. In the case of JS5 response,
     * since the JS5 protocol only has a single possible response, there is no opcode system
     * supported for it. As such, we use a filler opcode of `0` to fit our needs.
     */
    public fun incrementOutgoingPacketOpcode(
        hostAddress: String,
        opcode: Int,
    )

    /**
     * Increments the outgoing packet payload sent to the provided [String].
     * @param inetAddress the address to which a packet was sent.
     * @param opcode the opcode of the packet which was sent. The opcodes
     * correspond to the ones found in the client. The opcode itself will
     * not be incremented in this function - it is only needed to determine
     * which opcode's payload value we need to increase.
     * @param payloadSize the number of bytes that the payload is made out of.
     * It should be noted that the payload size _only_ includes the payload, and
     * not the 1-2 bytes for the opcode, nor the 1-2 bytes for the value defining
     * the payload size. Both of those values can be retroactively determined,
     * if more accurate metrics are required. In any case, however, due to the TCP
     * protocol having a significant amount of overhead for its packets on-top of it,
     * the metrics would never be truly accurate in terms of the data received.
     */
    public fun incrementOutgoingPacketPayload(
        hostAddress: String,
        opcode: Int,
        payloadSize: Int,
    )

    /**
     * The local datetime when the traffic began measuring. This value will be reset
     * whenever the [resetTransient] function is called.
     * @return the local datetime when the traffic began measuring, or was last reset.
     */
    public fun startDateTime(): LocalDateTime

    /**
     * The duration for how long we've currently been measuring the traffic in this channel.
     * This value is essentially just [LocalDateTime.now] minus [startDateTime].
     * The [JvmSynthetic] annotation is implied, since [Duration] is a value class, it would not
     * be visible from Java anyway.
     * @return the duration that has elapsed since the traffic began measuring, or was last reset.
     */
    @JvmSynthetic
    public fun elapsed(): Duration

    /**
     * The duration for how long we've currently been measuring the traffic in this channel, in milliseconds.
     * This value is essentially just [LocalDateTime.now] minus [startDateTime].
     * @return the duration that has elapsed since the traffic began measuring, or was last reset, in milliseconds.
     */
    public fun elapsedMillis(): Long

    /**
     * Captures a snapshot of the traffic in this channel since the last reset (or when it began measuring,
     * if it has never been reset). This function will not synchronize or reset any of the metrics.
     * @return a snapshot of the channel's traffic since the last reset, or when it first began measuring.
     */
    public fun snapshot(): ChannelTrafficSnapshot

    /**
     * Resets all the transient channel traffic values, and returns a snapshot of what the state was
     * just before the resetting.
     *
     * Note that not all values can be reset. Metrics such as active connections will remain untouched,
     * as resetting those would de-synchronize our results, since any connections that were active
     * before the result would now be unknown. If any of those channels would disconnect after,
     * the counter would go negative.
     * @return a snapshot of the channel's traffic since the last reset, or when it first began measuring.
     */
    public fun resetTransient(): ChannelTrafficSnapshot

    /**
     * Freezes tracking of any transient metrics in this channel's traffic monitor. While frozen,
     * metrics such as incoming packets, outgoing packets and disconnections will not be updated.
     * Active connections, however, are not impacted by the freezes, and will continue to monitor
     * any connected channels.
     */
    public fun freeze()

    /**
     * Un-freezes tracking of any transient metrics in this channel's traffic monitor. Once unfrozen,
     * the transient information such as incoming packets, outgoing packets and disconnections
     * will continue to be added to the traffic monitor.
     */
    public fun unfreeze()
}
