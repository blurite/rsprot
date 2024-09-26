package net.rsprot.protocol.metrics.channel.snapshots.util

import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.metrics.channel.snapshots.InetAddressSnapshot
import java.util.concurrent.atomic.AtomicIntegerArray
import java.util.concurrent.atomic.AtomicLongArray

public class InetAddressTrafficCounter<CP, SP, DC>(
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

    public fun addDisconnectionReason(reason: Int) {
        disconnectionsByReason.incrementAndGet(reason)
    }

    public fun incrementIncomingPackets(
        opcode: Int,
        payloadSize: Int,
    ) {
        incomingPackets.incrementAndGet(opcode)
        trafficByIncomingPackets.addAndGet(opcode, payloadSize.toLong())
    }

    public fun incrementOutgoingPackets(
        opcode: Int,
        payloadSize: Int,
    ) {
        outgoingPackets.incrementAndGet(opcode)
        trafficByOutgoingPackets.addAndGet(opcode, payloadSize.toLong())
    }

    public fun incrementOutgoingPacketPayload(
        opcode: Int,
        payloadSize: Int,
    ) {
        // Only increment the payload size here, this is for JS5 where the responses are fragmented
        // (e.g. 1 opcode but the payload is split into 100 individual chunks)
        trafficByOutgoingPackets.addAndGet(opcode, payloadSize.toLong())
    }

    public fun incrementOutgoingPacketOpcode(opcode: Int) {
        outgoingPackets.incrementAndGet(opcode)
    }

    public fun snapshot(): InetAddressSnapshot<CP, SP, DC> {
        val oldDisconnectionsByReason: AtomicIntegerArray = this.disconnectionsByReason
        val oldIncomingPackets: AtomicLongArray = this.incomingPackets
        val oldTrafficByIncomingPackets: AtomicLongArray = this.trafficByIncomingPackets
        val oldOutgoingPackets: AtomicLongArray = this.outgoingPackets
        val oldTrafficByOutgoingPackets: AtomicLongArray = this.trafficByOutgoingPackets
        disconnectionsByReason = AtomicIntegerArray(oldDisconnectionsByReason.length())
        incomingPackets = AtomicLongArray(oldIncomingPackets.length())
        trafficByIncomingPackets = AtomicLongArray(oldTrafficByIncomingPackets.length())
        outgoingPackets = AtomicLongArray(oldOutgoingPackets.length())
        trafficByOutgoingPackets = AtomicLongArray(oldTrafficByOutgoingPackets.length())
        val disconnectionsByReason =
            buildMap {
                for (i in disconnectionReasons.indices) {
                    put(disconnectionReasons[i], oldDisconnectionsByReason.get(i))
                }
            }
        val incomingPackets =
            buildMap {
                for (prot in clientProts) {
                    put(
                        prot,
                        PacketSnapshot(
                            oldIncomingPackets.get(prot.opcode),
                            oldTrafficByIncomingPackets.get(prot.opcode),
                        ),
                    )
                }
            }
        val outgoingPackets =
            buildMap {
                for (prot in serverProts) {
                    put(
                        prot,
                        PacketSnapshot(
                            oldOutgoingPackets.get(prot.opcode),
                            oldTrafficByOutgoingPackets.get(prot.opcode),
                        ),
                    )
                }
            }
        return InetAddressSnapshot(
            disconnectionsByReason,
            incomingPackets,
            outgoingPackets,
        )
    }
}
