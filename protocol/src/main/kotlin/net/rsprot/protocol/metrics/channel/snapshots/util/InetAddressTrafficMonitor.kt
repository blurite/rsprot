package net.rsprot.protocol.metrics.channel.snapshots.util

import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.metrics.channel.snapshots.InetAddressSnapshot
import java.util.EnumMap
import java.util.concurrent.atomic.AtomicIntegerArray
import java.util.concurrent.atomic.AtomicLongArray

/**
 * A traffic monitor for a specific [java.net.InetAddress].
 * @property clientProts an array of client prots for a specific channel type (login, JS5, game).
 * @property serverProts an array of server prots for a specific channel type (login, JS5, game).
 * @property disconnectionReasons an array of enum constants that represent possible disconnection reasons
 * behind why a connection was halted.
 * @property disconnectionsByReason an atomic integer array that tracks the number of times each
 * disconnection reason was observed for this [java.net.InetAddress].
 * @property incomingPackets an atomic long array that tracks the number of times each opcode was
 * received from the provided [java.net.InetAddress].
 * @property trafficByIncomingPackets an atomic long array that tracks the sum of the payloads of
 * each of the incoming packets. Note that this metric only tracks the payload size, and not the 1-2 bytes
 * for the opcode, nor for the size marker.
 * @property outgoingPackets an atomic long array that tracks the number of times each opcode was
 * sent to the provided [java.net.InetAddress].
 * @property trafficByOutgoingPackets an atomic long array that tracks the sum of the payloads
 * of each of the outgoing packets. Note that this metric only tracks the payload size, and not the 1-2 bytes
 *  * for the opcode, nor for the size marker.
 */
public class InetAddressTrafficMonitor<CP, SP, DC>(
    private val clientProts: Array<out CP>,
    private val serverProts: Array<out SP>,
    private val disconnectionReasons: Array<out DC>,
) where CP : ClientProt, CP : Enum<CP>, SP : ServerProt, SP : Enum<SP>, DC : Enum<DC> {
    @Volatile
    private var disconnectionsByReason: AtomicIntegerArray = AtomicIntegerArray(disconnectionReasons.size)

    @Volatile
    private var incomingPackets: AtomicLongArray = AtomicLongArray(clientProts.maxOf { it.opcode } + 1)

    @Volatile
    private var trafficByIncomingPackets: AtomicLongArray = AtomicLongArray(clientProts.maxOf { it.opcode } + 1)

    @Volatile
    private var outgoingPackets: AtomicLongArray = AtomicLongArray(serverProts.maxOf { it.opcode } + 1)

    @Volatile
    private var trafficByOutgoingPackets: AtomicLongArray = AtomicLongArray(serverProts.maxOf { it.opcode } + 1)

    /**
     * Increments the specified [reason] for why a connection was lost to the specified [java.net.InetAddress].
     * @param reason the numeric representation of the [Enum.ordinal] value via the [disconnectionReasons].
     */
    public fun addDisconnectionReason(reason: Int) {
        disconnectionsByReason.incrementAndGet(reason)
    }

    /**
     * Increments the incoming packets received from this [java.net.InetAddress].
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
        opcode: Int,
        payloadSize: Int,
    ) {
        incomingPackets.incrementAndGet(opcode)
        trafficByIncomingPackets.addAndGet(opcode, payloadSize.toLong())
    }

    /**
     * Increments the outgoing packets sent to this [java.net.InetAddress].
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
        opcode: Int,
        payloadSize: Int,
    ) {
        outgoingPackets.incrementAndGet(opcode)
        trafficByOutgoingPackets.addAndGet(opcode, payloadSize.toLong())
    }

    /**
     * Increments the outgoing packet payload sent to this [java.net.InetAddress].
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
        opcode: Int,
        payloadSize: Int,
    ) {
        // Only increment the payload size here, this is for JS5 where the responses are fragmented
        // (e.g. 1 opcode but the payload is split into 100 individual chunks)
        trafficByOutgoingPackets.addAndGet(opcode, payloadSize.toLong())
    }

    /**
     * Increments the outgoing packet opcode counter alone, without the payload increase.
     * We require a special function for this since our JS5Service implementation fragments
     * the payloads into blocks of X bytes. Due to the nature of it, we may only have a
     * single packet header, but a hundred slices of that packet's payload.
     * @param opcode the opcode of the packet which was sent. In the case of JS5 response,
     * since the JS5 protocol only has a single possible response, there is no opcode system
     * supported for it. As such, we use a filler opcode of `0` to fit our needs.
     */
    public fun incrementOutgoingPacketOpcode(opcode: Int) {
        outgoingPackets.incrementAndGet(opcode)
    }

    /**
     * Captures a snapshot of the traffic in this channel since the last reset (or when it began measuring,
     * if it has never been reset). This function will also reset the tracked metrics.
     * @return a snapshot of the channel's traffic since the last reset, or when it first began measuring.
     */
    public fun snapshot(): InetAddressSnapshot<CP, SP, DC> {
        val oldDisconnectionsByReason: AtomicIntegerArray = this.disconnectionsByReason
        val oldIncomingPackets: AtomicLongArray = this.incomingPackets
        val oldTrafficByIncomingPackets: AtomicLongArray = this.trafficByIncomingPackets
        val oldOutgoingPackets: AtomicLongArray = this.outgoingPackets
        val oldTrafficByOutgoingPackets: AtomicLongArray = this.trafficByOutgoingPackets
        this@InetAddressTrafficMonitor.disconnectionsByReason = AtomicIntegerArray(oldDisconnectionsByReason.length())
        incomingPackets = AtomicLongArray(oldIncomingPackets.length())
        trafficByIncomingPackets = AtomicLongArray(oldTrafficByIncomingPackets.length())
        outgoingPackets = AtomicLongArray(oldOutgoingPackets.length())
        trafficByOutgoingPackets = AtomicLongArray(oldTrafficByOutgoingPackets.length())
        val disconnectionsByReason = EnumMap<DC, Int>(disconnectionReasons[0].javaClass)
        for (i in disconnectionReasons.indices) {
            disconnectionsByReason[disconnectionReasons[i]] = oldDisconnectionsByReason.get(i)
        }
        val incomingPackets = EnumMap<CP, PacketSnapshot>(clientProts[0].javaClass)
        for (prot in clientProts) {
            if (prot.opcode < 0) continue
            incomingPackets[prot] =
                PacketSnapshot(
                    oldIncomingPackets.get(prot.opcode),
                    oldTrafficByIncomingPackets.get(prot.opcode),
                )
        }
        val outgoingPackets = EnumMap<SP, PacketSnapshot>(serverProts[0].javaClass)
        for (prot in serverProts) {
            if (prot.opcode < 0) continue
            outgoingPackets[prot] =
                PacketSnapshot(
                    oldOutgoingPackets.get(prot.opcode),
                    oldTrafficByOutgoingPackets.get(prot.opcode),
                )
        }
        return InetAddressSnapshot(
            disconnectionsByReason,
            incomingPackets,
            outgoingPackets,
        )
    }
}
