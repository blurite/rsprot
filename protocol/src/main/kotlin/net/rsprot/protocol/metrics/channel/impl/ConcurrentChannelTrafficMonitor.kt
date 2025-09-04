package net.rsprot.protocol.metrics.channel.impl

import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.metrics.channel.ChannelTrafficMonitor
import net.rsprot.protocol.metrics.channel.snapshots.impl.ConcurrentChannelTrafficSnapshot
import net.rsprot.protocol.metrics.channel.snapshots.util.InetAddressTrafficMonitor
import net.rsprot.protocol.metrics.lock.TrafficMonitorLock
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * A concurrent implementation of a channel's traffic monitor.
 * @property lock a traffic monitor lock that is used when the [resetTransient] function is invoked,
 * in order to ensure consistency in the data.
 * @property clientProts an array of client prots for this channel.
 * @property serverProts an array of server prots for this channel.
 * @property disconnectionReasons an array of disconnection reasons for this channel.
 * @property startDateTime the local datetime when this traffic monitor began tracking,
 * or was last reset.
 * @property activeConnectionsByAddress the active connections established per [String] basis.
 * @property hostAddressTrafficMonitors the traffic monitors per [String], tracking various
 * packets and disconnection reasons at a finer level.
 * @property frozen whether the transient properties are frozen, meaning any changes to them
 * are discarded. Anything stateful will continue to be modified.
 */
public class ConcurrentChannelTrafficMonitor<CP, SP, DC>(
    private val lock: TrafficMonitorLock,
    private val clientProts: Array<out CP>,
    private val serverProts: Array<out SP>,
    private val disconnectionReasons: Array<out DC>,
    private var startDateTime: LocalDateTime = LocalDateTime.now(),
) : ChannelTrafficMonitor where CP : ClientProt, CP : Enum<CP>, SP : ServerProt, SP : Enum<SP>, DC : Enum<DC> {
    private val activeConnectionsByAddress: MutableMap<String, Int> = ConcurrentHashMap()
    private var inetAddressTrafficMonitors: MutableMap<String, InetAddressTrafficMonitor<CP, SP, DC>> =
        ConcurrentHashMap()

    @Volatile
    private var frozen: Boolean = false

    override fun incrementConnections(hostAddress: String) {
        lock.use {
            activeConnectionsByAddress.compute(hostAddress) { _, v ->
                (v ?: 0) + 1
            }
        }
    }

    override fun decrementConnections(hostAddress: String) {
        lock.use {
            activeConnectionsByAddress.compute(hostAddress) { _, v ->
                val result = (v ?: 0) - 1
                return@compute if (result <= 0) null else result
            }
        }
    }

    private fun getTrafficCounter(hostAddress: String): InetAddressTrafficMonitor<CP, SP, DC> =
        inetAddressTrafficMonitors.computeIfAbsent(hostAddress) {
            InetAddressTrafficMonitor(
                clientProts,
                serverProts,
                disconnectionReasons,
            )
        }

    override fun addDisconnectionReason(
        hostAddress: String,
        reason: Int,
    ) {
        if (frozen) return
        lock.use {
            if (reason in disconnectionReasons.indices) {
                val trafficCounter = getTrafficCounter(hostAddress)
                trafficCounter.addDisconnectionReason(reason)
            }
        }
    }

    override fun incrementIncomingPackets(
        hostAddress: String,
        opcode: Int,
        payloadSize: Int,
    ) {
        if (frozen) return
        lock.use {
            if (opcode in clientProts.indices) {
                val trafficCounter = getTrafficCounter(hostAddress)
                trafficCounter.incrementIncomingPackets(opcode, payloadSize)
            }
        }
    }

    override fun incrementOutgoingPackets(
        hostAddress: String,
        opcode: Int,
        payloadSize: Int,
    ) {
        if (frozen) return
        lock.use {
            if (opcode in serverProts.indices) {
                val trafficCounter = getTrafficCounter(hostAddress)
                trafficCounter.incrementOutgoingPackets(opcode, payloadSize)
            }
        }
    }

    override fun incrementOutgoingPacketPayload(
        hostAddress: String,
        opcode: Int,
        payloadSize: Int,
    ) {
        if (frozen) return
        lock.use {
            if (opcode in serverProts.indices) {
                val trafficCounter = getTrafficCounter(hostAddress)
                trafficCounter.incrementOutgoingPacketPayload(opcode, payloadSize)
            }
        }
    }

    override fun incrementOutgoingPacketOpcode(
        hostAddress: String,
        opcode: Int,
    ) {
        if (frozen) return
        lock.use {
            if (opcode in serverProts.indices) {
                val trafficCounter = getTrafficCounter(hostAddress)
                trafficCounter.incrementOutgoingPacketOpcode(opcode)
            }
        }
    }

    override fun startDateTime(): LocalDateTime = lock.use { this.startDateTime }

    override fun elapsed(): Duration = elapsedMillis().milliseconds

    override fun elapsedMillis(): Long =
        lock.use {
            ChronoUnit.MILLIS.between(LocalDateTime.now(), startDateTime)
        }

    override fun snapshot(): ConcurrentChannelTrafficSnapshot<CP, SP, DC> {
        lock.use {
            val now = LocalDateTime.now()
            val activeConnectionsByAddress: Map<String, Int> = this.activeConnectionsByAddress.toMap()
            val inetAddressTrafficMonitors: Map<String, InetAddressTrafficMonitor<CP, SP, DC>> =
                this
                    .inetAddressTrafficMonitors
                    .toMap()
            val inetAddressSnapshots =
                inetAddressTrafficMonitors.mapValues { entry ->
                    entry.value.snapshot()
                }
            return ConcurrentChannelTrafficSnapshot(
                this.startDateTime,
                now,
                activeConnectionsByAddress,
                inetAddressSnapshots,
            )
        }
    }

    override fun resetTransient(): ConcurrentChannelTrafficSnapshot<CP, SP, DC> {
        var oldStart: LocalDateTime
        var newStart: LocalDateTime
        var activeConnectionsByAddress: Map<String, Int>
        var inetAddressTrafficMonitors: Map<String, InetAddressTrafficMonitor<CP, SP, DC>>
        // Synchronize during the reallocation to ensure _some_ kind of consistency
        // This won't be perfect, but it should avoid most cases of inconsistency
        // where mutations are performed while we are clearing the transient data
        // Any thread that was actively modifying and had a local copy of
        lock.transfer {
            oldStart = this.startDateTime
            activeConnectionsByAddress = this.activeConnectionsByAddress.toMap()
            inetAddressTrafficMonitors = this.inetAddressTrafficMonitors.toMap()
            newStart = LocalDateTime.now()
            this.startDateTime = newStart
            this.inetAddressTrafficMonitors = ConcurrentHashMap()
        }
        val inetAddressSnapshots =
            inetAddressTrafficMonitors.mapValues { entry ->
                entry.value.snapshot()
            }
        return ConcurrentChannelTrafficSnapshot(
            oldStart,
            newStart,
            activeConnectionsByAddress,
            inetAddressSnapshots,
        )
    }

    override fun freeze() {
        this.frozen = true
    }

    override fun unfreeze() {
        this.frozen = false
    }
}
