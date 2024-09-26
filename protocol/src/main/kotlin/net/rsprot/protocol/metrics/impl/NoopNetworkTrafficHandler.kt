package net.rsprot.protocol.metrics.impl

import net.rsprot.protocol.metrics.NetworkTrafficHandler
import net.rsprot.protocol.metrics.channel.impl.GameChannelTrafficHandler
import net.rsprot.protocol.metrics.channel.impl.Js5ChannelTrafficHandler
import net.rsprot.protocol.metrics.channel.impl.LoginChannelTrafficHandler
import net.rsprot.protocol.metrics.channel.impl.NoopChannelTrafficHandler
import net.rsprot.protocol.metrics.snapshots.NetworkTrafficSnapshot
import net.rsprot.protocol.metrics.snapshots.impl.NoopNetworkTrafficSnapshot

public data object NoopNetworkTrafficHandler : NetworkTrafficHandler {
    override val loginChannelTrafficHandler: LoginChannelTrafficHandler =
        LoginChannelTrafficHandler(NoopChannelTrafficHandler)
    override val js5ChannelTrafficHandler: Js5ChannelTrafficHandler =
        Js5ChannelTrafficHandler(NoopChannelTrafficHandler)
    override val gameChannelTrafficHandler: GameChannelTrafficHandler =
        GameChannelTrafficHandler(NoopChannelTrafficHandler)

    override fun incrementConnections() {
    }

    override fun snapshot(): NetworkTrafficSnapshot = NoopNetworkTrafficSnapshot

    override fun resetTransient(): NetworkTrafficSnapshot = NoopNetworkTrafficSnapshot
}
