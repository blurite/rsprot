package net.rsprot.protocol.metrics.channel.impl

import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.metrics.channel.ChannelTrafficHandler
import net.rsprot.protocol.metrics.channel.snapshots.impl.GenericChannelTrafficSnapshot
import net.rsprot.protocol.metrics.channel.snapshots.util.InetAddressTrafficCounter
import net.rsprot.protocol.metrics.lock.TrafficHandlerLock
import java.net.InetAddress
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

public class GenericChannelTrafficHandler<CP, SP, DC>(
    private val lock: TrafficHandlerLock,
    private val clientProts: Array<out CP>,
    private val serverProts: Array<out SP>,
    private val disconnectionReasons: Array<out DC>,
    private var startDateTime: LocalDateTime = LocalDateTime.now(),
) : ChannelTrafficHandler where CP : ClientProt, CP : Enum<CP>, SP : ServerProt, SP : Enum<SP>, DC : Enum<DC> {
    private val activeConnectionsByAddress: MutableMap<InetAddress, Int> = ConcurrentHashMap()
    private val totalActiveConnections: AtomicInteger = AtomicInteger(0)
    private var inetAddressTrafficCounters: MutableMap<InetAddress, InetAddressTrafficCounter<CP, SP, DC>> =
        ConcurrentHashMap()

    @Volatile
    private var frozen: Boolean = false

    override fun incrementConnections(inetAddress: InetAddress) {
        lock.use {
            totalActiveConnections.incrementAndGet()
            activeConnectionsByAddress.compute(inetAddress) { _, v ->
                (v ?: 0) + 1
            }
        }
    }

    override fun decrementConnections(inetAddress: InetAddress) {
        lock.use {
            totalActiveConnections.decrementAndGet()
            activeConnectionsByAddress.compute(inetAddress) { _, v ->
                val result = (v ?: 0) - 1
                return@compute if (result <= 0) null else result
            }
        }
    }

    private fun getTrafficCounter(inetAddress: InetAddress): InetAddressTrafficCounter<CP, SP, DC> =
        inetAddressTrafficCounters.computeIfAbsent(inetAddress) {
            InetAddressTrafficCounter(
                clientProts,
                serverProts,
                disconnectionReasons,
            )
        }

    override fun addDisconnectionReason(
        inetAddress: InetAddress,
        reason: Int,
    ) {
        if (frozen) return
        lock.use {
            if (reason in disconnectionReasons.indices) {
                val trafficCounter = getTrafficCounter(inetAddress)
                trafficCounter.addDisconnectionReason(reason)
            }
        }
    }

    override fun incrementIncomingPackets(
        inetAddress: InetAddress,
        opcode: Int,
        payloadSize: Int,
    ) {
        if (frozen) return
        lock.use {
            if (opcode in clientProts.indices) {
                val trafficCounter = getTrafficCounter(inetAddress)
                trafficCounter.incrementIncomingPackets(opcode, payloadSize)
            }
        }
    }

    override fun incrementOutgoingPackets(
        inetAddress: InetAddress,
        opcode: Int,
        payloadSize: Int,
    ) {
        if (frozen) return
        lock.use {
            if (opcode in serverProts.indices) {
                val trafficCounter = getTrafficCounter(inetAddress)
                trafficCounter.incrementOutgoingPackets(opcode, payloadSize)
            }
        }
    }

    override fun incrementOutgoingPacketPayload(
        inetAddress: InetAddress,
        opcode: Int,
        payloadSize: Int,
    ) {
        if (frozen) return
        lock.use {
            if (opcode in serverProts.indices) {
                val trafficCounter = getTrafficCounter(inetAddress)
                trafficCounter.incrementOutgoingPacketPayload(opcode, payloadSize)
            }
        }
    }

    override fun incrementOutgoingPacketOpcode(
        inetAddress: InetAddress,
        opcode: Int,
    ) {
        if (frozen) return
        lock.use {
            if (opcode in serverProts.indices) {
                val trafficCounter = getTrafficCounter(inetAddress)
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

    override fun snapshot(): GenericChannelTrafficSnapshot<CP, SP, DC> {
        lock.use {
            val now = LocalDateTime.now()
            val activeConnectionsByAddress: Map<InetAddress, Int> = this.activeConnectionsByAddress.toMap()
            val totalActiveConnections: Int = this.totalActiveConnections.get()
            val inetAddressTrafficCounters: Map<InetAddress, InetAddressTrafficCounter<CP, SP, DC>> =
                this
                    .inetAddressTrafficCounters
                    .toMap()
            val inetAddressSnapshots =
                inetAddressTrafficCounters.mapValues { entry ->
                    entry.value.snapshot()
                }
            return GenericChannelTrafficSnapshot(
                this.startDateTime,
                now,
                activeConnectionsByAddress,
                totalActiveConnections,
                inetAddressSnapshots,
            )
        }
    }

    override fun resetTransient(): GenericChannelTrafficSnapshot<CP, SP, DC> {
        var oldStart: LocalDateTime
        var newStart: LocalDateTime
        var activeConnectionsByAddress: Map<InetAddress, Int>
        var totalActiveConnections: Int
        var inetAddressTrafficCounters: Map<InetAddress, InetAddressTrafficCounter<CP, SP, DC>>
        // Synchronize during the reallocation to ensure _some_ kind of consistency
        // This won't be perfect, but it should avoid most cases of inconsistency
        // where mutations are performed while we are clearing the transient data
        // Any thread that was actively modifying and had a local copy of
        lock.transfer {
            oldStart = this.startDateTime
            activeConnectionsByAddress = this.activeConnectionsByAddress.toMap()
            totalActiveConnections = this.totalActiveConnections.get()
            inetAddressTrafficCounters = this.inetAddressTrafficCounters.toMap()
            newStart = LocalDateTime.now()
            this.startDateTime = newStart
            this.inetAddressTrafficCounters = ConcurrentHashMap()
        }
        val inetAddressSnapshots =
            inetAddressTrafficCounters.mapValues { entry ->
                entry.value.snapshot()
            }
        return GenericChannelTrafficSnapshot(
            oldStart,
            newStart,
            activeConnectionsByAddress,
            totalActiveConnections,
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
