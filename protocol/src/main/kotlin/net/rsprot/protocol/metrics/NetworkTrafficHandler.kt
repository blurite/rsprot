package net.rsprot.protocol.metrics

import net.rsprot.protocol.metrics.channel.impl.GameChannelTrafficHandler
import net.rsprot.protocol.metrics.channel.impl.Js5ChannelTrafficHandler
import net.rsprot.protocol.metrics.channel.impl.LoginChannelTrafficHandler
import net.rsprot.protocol.metrics.snapshots.NetworkTrafficSnapshot
import java.net.InetAddress

/**
 * A complete network traffic handler that covers all channel types.
 * @property loginChannelTrafficHandler the traffic handler for the login channel,
 * including the "handshake" phase as it is commonly referred to.
 * @property js5ChannelTrafficHandler the JS5 channel traffic handler.
 * @property gameChannelTrafficHandler the game channel traffic handler.
 */
public interface NetworkTrafficHandler<in LoginBlock> {
    public val loginChannelTrafficHandler: LoginChannelTrafficHandler
    public val js5ChannelTrafficHandler: Js5ChannelTrafficHandler
    public val gameChannelTrafficHandler: GameChannelTrafficHandler

    /**
     * Increments connections established in total.
     */
    public fun incrementConnections()

    /**
     * Adds a new login block from the provided [inetAddress].
     * @param inetAddress the address from which a complete login block was received.
     * @param block the login block that was received.
     */
    public fun addLoginBlock(
        inetAddress: InetAddress,
        block: LoginBlock,
    )

    /**
     * Creates a full network traffic snapshot covering all three of the channel traffic
     * handlers. This snapshot function will not use synchronization during creation,
     * so it is possible for slight inconsistencies to occur due to the data being cloned
     * at slightly different moments.
     * @return a full network traffic snapshot, covering any activity in all three channels
     * since it was last reset (or began monitoring).
     */
    public fun snapshot(): NetworkTrafficSnapshot

    /**
     * Resets any transient metrics and captures a full traffic snapshot of everything that
     * happened prior to the resetting. This snapshot function will use a short-lived
     * synchronization block to ensure consistency in the data captured.
     * @return a full network traffic snapshot, covering any activity in all three channels
     * since it was last reset (or began monitoring).
     */
    public fun resetTransient(): NetworkTrafficSnapshot

    /**
     * Freezes any transient traffic monitoring for this handler, across all three of the
     * channel handlers. The freeze function does not stop active connection tracking,
     * however, as that could cause bad data in the future (number of connections going
     * negative). This will block majority of the monitoring that goes on, though.
     * This function has no effect if the traffic was already frozen.
     */
    public fun freeze()

    /**
     * Unfreezes any transient traffic monitoring that was frozen before.
     * This function has no effect if the traffic was not frozen.
     */
    public fun unfreeze()
}
