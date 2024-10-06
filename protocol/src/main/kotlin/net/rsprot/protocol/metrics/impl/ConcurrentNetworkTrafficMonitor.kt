package net.rsprot.protocol.metrics.impl

import net.rsprot.protocol.metrics.NetworkTrafficMonitor
import net.rsprot.protocol.metrics.channel.impl.GameChannelTrafficMonitor
import net.rsprot.protocol.metrics.channel.impl.Js5ChannelTrafficMonitor
import net.rsprot.protocol.metrics.channel.impl.LoginChannelTrafficMonitor
import net.rsprot.protocol.metrics.lock.TrafficMonitorLock
import net.rsprot.protocol.metrics.snapshots.impl.ConcurrentNetworkTrafficSnapshot
import java.net.InetAddress
import java.time.LocalDateTime
import java.util.Queue
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicInteger

/**
 * A concurrent network traffic monitor, responsible for tracking all the data sent via
 * the [loginChannelTrafficMonitor], the [js5ChannelTrafficMonitor] and the [gameChannelTrafficMonitor].
 * In addition to those, it will track the number of connections established, as well as any
 * login complete login blocks that reach the server, based on the respective [InetAddress]
 * behind the login blocks. Latter can be used to link any abnormalities to specific individuals.
 *
 * While this implementation is _mostly_ atomic, it will shortly block during the [resetTransient]
 * function call, while it assigns new instances to all the properties behind this implementation.
 * This is only done to ensure consistency between the properties. As the blocking is extremely
 * short-lived, it should not cause any issues.
 *
 * @property lock the traffic monitor lock used to freeze any modifications temporarily while
 * new instances of the properties in this class are constructed, in order to ensure consistency
 * between the data. Any other operations in this class are based on Atomic properties,
 * or data structured found in the [java.util.concurrent] package.
 * @property loginChannelTrafficMonitor the channel traffic monitor for logins (and "handshakes",
 * as they are commonly referred to).
 * @property js5ChannelTrafficMonitor the channel traffic monitor for JS5.
 * @property gameChannelTrafficMonitor the channel traffic monitor for game connections,
 * essentially after a login request is accepted.
 * @property startDateTime the date time when the tracking was started. Note that this property
 * is reset to [LocalDateTime.now] whenever the [resetTransient] function is called.
 * @property connections the number of connections that were established in total since the traffic
 * began.
 * @property loginBlocks the complete login blocks received per [InetAddress].
 * @property frozen whether the tracking of [loginBlocks] is temporarily frozen.
 */
public class ConcurrentNetworkTrafficMonitor<LoginBlock>(
    private val lock: TrafficMonitorLock,
    override val loginChannelTrafficMonitor: LoginChannelTrafficMonitor,
    override val js5ChannelTrafficMonitor: Js5ChannelTrafficMonitor,
    override val gameChannelTrafficMonitor: GameChannelTrafficMonitor,
    private var startDateTime: LocalDateTime = LocalDateTime.now(),
) : NetworkTrafficMonitor<LoginBlock> {
    private var connections: AtomicInteger = AtomicInteger(0)
    private var loginBlocks: MutableMap<InetAddress, Queue<LoginBlock>> = ConcurrentHashMap()

    @Volatile
    private var frozen: Boolean = false

    override fun incrementConnections() {
        connections.incrementAndGet()
    }

    override fun addLoginBlock(
        inetAddress: InetAddress,
        block: LoginBlock,
    ) {
        if (frozen) return
        val queue =
            loginBlocks.computeIfAbsent(inetAddress) {
                ConcurrentLinkedQueue()
            }
        queue.add(block)
    }

    override fun snapshot(): ConcurrentNetworkTrafficSnapshot<LoginBlock> {
        val newStart = LocalDateTime.now()
        val loginBlocks =
            this.loginBlocks.entries.associate {
                it.key to it.value.toList()
            }
        return ConcurrentNetworkTrafficSnapshot(
            this.startDateTime,
            newStart,
            connections.get(),
            loginBlocks,
            loginChannelTrafficMonitor.snapshot(),
            js5ChannelTrafficMonitor.snapshot(),
            gameChannelTrafficMonitor.snapshot(),
        )
    }

    override fun resetTransient(): ConcurrentNetworkTrafficSnapshot<LoginBlock> {
        var oldStart: LocalDateTime
        var oldConnections: AtomicInteger
        var oldLoginBlocks: Map<InetAddress, Queue<LoginBlock>>
        lock.transfer {
            oldStart = this.startDateTime
            oldConnections = this.connections
            oldLoginBlocks = this.loginBlocks
            this.startDateTime = LocalDateTime.now()
            this.connections = AtomicInteger(0)
            this.loginBlocks = ConcurrentHashMap()
        }
        val loginBlocks =
            oldLoginBlocks.entries.associate {
                it.key to it.value.toList()
            }
        return ConcurrentNetworkTrafficSnapshot(
            oldStart,
            this.startDateTime,
            oldConnections.get(),
            loginBlocks,
            loginChannelTrafficMonitor.resetTransient(),
            js5ChannelTrafficMonitor.resetTransient(),
            gameChannelTrafficMonitor.resetTransient(),
        )
    }

    override fun freeze() {
        this.frozen = true
        this.loginChannelTrafficMonitor.freeze()
        this.js5ChannelTrafficMonitor.freeze()
        this.gameChannelTrafficMonitor.freeze()
    }

    override fun unfreeze() {
        this.frozen = false
        this.loginChannelTrafficMonitor.unfreeze()
        this.js5ChannelTrafficMonitor.unfreeze()
        this.gameChannelTrafficMonitor.unfreeze()
    }
}
