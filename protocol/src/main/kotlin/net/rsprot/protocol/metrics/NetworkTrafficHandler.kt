package net.rsprot.protocol.metrics

import net.rsprot.protocol.metrics.channel.impl.GameChannelTrafficHandler
import net.rsprot.protocol.metrics.channel.impl.Js5ChannelTrafficHandler
import net.rsprot.protocol.metrics.channel.impl.LoginChannelTrafficHandler
import net.rsprot.protocol.metrics.snapshots.NetworkTrafficSnapshot
import java.net.InetAddress

public interface NetworkTrafficHandler<in LoginBlock> {
    public val loginChannelTrafficHandler: LoginChannelTrafficHandler
    public val js5ChannelTrafficHandler: Js5ChannelTrafficHandler
    public val gameChannelTrafficHandler: GameChannelTrafficHandler

    public fun incrementConnections()

    public fun addLoginBlock(
        inetAddress: InetAddress,
        block: LoginBlock,
    )

    public fun snapshot(): NetworkTrafficSnapshot

    public fun resetTransient(): NetworkTrafficSnapshot

    public fun freeze()

    public fun unfreeze()
}
