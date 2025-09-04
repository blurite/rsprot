package net.rsprot.protocol.metrics.impl

import net.rsprot.protocol.metrics.NetworkTrafficMonitor
import net.rsprot.protocol.metrics.channel.impl.GameChannelTrafficMonitor
import net.rsprot.protocol.metrics.channel.impl.Js5ChannelTrafficMonitor
import net.rsprot.protocol.metrics.channel.impl.LoginChannelTrafficMonitor
import net.rsprot.protocol.metrics.channel.impl.NoopChannelTrafficMonitor
import net.rsprot.protocol.metrics.snapshots.impl.NoopNetworkTrafficSnapshot
import java.net.SocketAddress

/**
 * A no-operation implementation of a [NetworkTrafficMonitor].
 * All functions in this traffic monitor lead to no operation taking place.
 */
public data object NoopNetworkTrafficMonitor : NetworkTrafficMonitor<Any?> {
    override val loginChannelTrafficMonitor: LoginChannelTrafficMonitor =
        LoginChannelTrafficMonitor(NoopChannelTrafficMonitor)
    override val js5ChannelTrafficMonitor: Js5ChannelTrafficMonitor =
        Js5ChannelTrafficMonitor(NoopChannelTrafficMonitor)
    override val gameChannelTrafficMonitor: GameChannelTrafficMonitor =
        GameChannelTrafficMonitor(NoopChannelTrafficMonitor)

    override fun incrementConnections() {
    }

    override fun addLoginBlock(
        socketAddress: SocketAddress,
        block: Any?,
    ) {
    }

    override fun snapshot(): NoopNetworkTrafficSnapshot = NoopNetworkTrafficSnapshot

    override fun resetTransient(): NoopNetworkTrafficSnapshot = NoopNetworkTrafficSnapshot

    override fun freeze() {
    }

    override fun unfreeze() {
    }
}
