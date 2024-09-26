package net.rsprot.protocol.metrics.impl

import net.rsprot.protocol.metrics.NetworkTrafficHandler
import net.rsprot.protocol.metrics.channel.impl.GameChannelTrafficHandler
import net.rsprot.protocol.metrics.channel.impl.Js5ChannelTrafficHandler
import net.rsprot.protocol.metrics.channel.impl.LoginChannelTrafficHandler
import net.rsprot.protocol.metrics.lock.TrafficHandlerLock
import net.rsprot.protocol.metrics.snapshots.NetworkTrafficSnapshot
import net.rsprot.protocol.metrics.snapshots.impl.GenericNetworkTrafficSnapshot
import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicInteger

public class GenericNetworkTrafficHandler(
    private val lock: TrafficHandlerLock,
    override var loginChannelTrafficHandler: LoginChannelTrafficHandler,
    override var js5ChannelTrafficHandler: Js5ChannelTrafficHandler,
    override var gameChannelTrafficHandler: GameChannelTrafficHandler,
    private var startDateTime: LocalDateTime = LocalDateTime.now(),
) : NetworkTrafficHandler {
    private var connections: AtomicInteger = AtomicInteger(0)

    override fun incrementConnections() {
        connections.incrementAndGet()
    }

    override fun snapshot(): GenericNetworkTrafficSnapshot {
        val newStart = LocalDateTime.now()
        return GenericNetworkTrafficSnapshot(
            this.startDateTime,
            newStart,
            connections.get(),
            loginChannelTrafficHandler.snapshot(),
            js5ChannelTrafficHandler.snapshot(),
            gameChannelTrafficHandler.snapshot(),
        )
    }

    override fun resetTransient(): NetworkTrafficSnapshot {
        var oldStart: LocalDateTime
        var oldConnections: AtomicInteger
        lock.transfer {
            oldStart = this.startDateTime
            oldConnections = this.connections
            this.startDateTime = LocalDateTime.now()
            this.connections = AtomicInteger(0)
        }
        return GenericNetworkTrafficSnapshot(
            oldStart,
            this.startDateTime,
            oldConnections.get(),
            loginChannelTrafficHandler.resetTransient(),
            js5ChannelTrafficHandler.resetTransient(),
            gameChannelTrafficHandler.resetTransient(),
        )
    }
}
