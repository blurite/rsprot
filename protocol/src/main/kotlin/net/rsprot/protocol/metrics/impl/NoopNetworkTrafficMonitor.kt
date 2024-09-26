package net.rsprot.protocol.metrics.impl

import net.rsprot.protocol.metrics.NetworkTrafficMonitor
import net.rsprot.protocol.metrics.channel.impl.GameChannelTrafficMonitor
import net.rsprot.protocol.metrics.channel.impl.Js5ChannelTrafficMonitor
import net.rsprot.protocol.metrics.channel.impl.LoginChannelTrafficMonitor
import net.rsprot.protocol.metrics.channel.impl.NoopChannelTrafficMonitor
import net.rsprot.protocol.metrics.snapshots.NetworkTrafficSnapshot
import net.rsprot.protocol.metrics.snapshots.impl.NoopNetworkTrafficSnapshot
import java.net.InetAddress

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
        inetAddress: InetAddress,
        block: Any?,
    ) {
    }

    override fun snapshot(): NetworkTrafficSnapshot = NoopNetworkTrafficSnapshot

    override fun resetTransient(): NetworkTrafficSnapshot = NoopNetworkTrafficSnapshot

    override fun freeze() {
    }

    override fun unfreeze() {
    }
}
