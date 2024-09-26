package net.rsprot.protocol.metrics.impl

import net.rsprot.protocol.metrics.NetworkTrafficHandler
import net.rsprot.protocol.metrics.channel.impl.GameChannelTrafficHandler
import net.rsprot.protocol.metrics.channel.impl.Js5ChannelTrafficHandler
import net.rsprot.protocol.metrics.channel.impl.LoginChannelTrafficHandler
import net.rsprot.protocol.metrics.lock.TrafficHandlerLock
import net.rsprot.protocol.metrics.snapshots.NetworkTrafficSnapshot
import net.rsprot.protocol.metrics.snapshots.impl.ConcurrentNetworkTrafficSnapshot
import java.net.InetAddress
import java.time.LocalDateTime
import java.util.Queue
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicInteger

public class ConcurrentNetworkTrafficHandler<LoginBlock>(
    private val lock: TrafficHandlerLock,
    override var loginChannelTrafficHandler: LoginChannelTrafficHandler,
    override var js5ChannelTrafficHandler: Js5ChannelTrafficHandler,
    override var gameChannelTrafficHandler: GameChannelTrafficHandler,
    private var startDateTime: LocalDateTime = LocalDateTime.now(),
) : NetworkTrafficHandler<LoginBlock> {
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
            loginChannelTrafficHandler.snapshot(),
            js5ChannelTrafficHandler.snapshot(),
            gameChannelTrafficHandler.snapshot(),
        )
    }

    override fun resetTransient(): NetworkTrafficSnapshot {
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
            loginChannelTrafficHandler.resetTransient(),
            js5ChannelTrafficHandler.resetTransient(),
            gameChannelTrafficHandler.resetTransient(),
        )
    }

    override fun freeze() {
        this.frozen = true
        this.loginChannelTrafficHandler.freeze()
        this.js5ChannelTrafficHandler.freeze()
        this.gameChannelTrafficHandler.freeze()
    }

    override fun unfreeze() {
        this.frozen = false
        this.loginChannelTrafficHandler.unfreeze()
        this.js5ChannelTrafficHandler.unfreeze()
        this.gameChannelTrafficHandler.unfreeze()
    }
}
