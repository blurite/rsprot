package net.rsprot.protocol.metrics

import net.rsprot.protocol.metrics.channel.impl.GameChannelTrafficMonitor
import net.rsprot.protocol.metrics.channel.impl.Js5ChannelTrafficMonitor
import net.rsprot.protocol.metrics.channel.impl.LoginChannelTrafficMonitor
import net.rsprot.protocol.metrics.snapshots.NetworkTrafficSnapshot

/**
 * A complete network traffic monitor that covers all channel types.
 * @property loginChannelTrafficMonitor the traffic monitor for the login channel,
 * including the "handshake" phase as it is commonly referred to.
 * @property js5ChannelTrafficMonitor the JS5 channel traffic monitor.
 * @property gameChannelTrafficMonitor the game channel traffic monitor.
 */
public interface NetworkTrafficMonitor<in LoginBlock> {
    public val loginChannelTrafficMonitor: LoginChannelTrafficMonitor
    public val js5ChannelTrafficMonitor: Js5ChannelTrafficMonitor
    public val gameChannelTrafficMonitor: GameChannelTrafficMonitor

    /**
     * Increments connections established in total.
     */
    public fun incrementConnections()

    /**
     * Adds a new login block from the provided [String].
     * @param inetAddress the address from which a complete login block was received.
     * @param block the login block that was received.
     */
    public fun addLoginBlock(
        hostAddress: String,
        block: LoginBlock,
    )

    /**
     * Creates a full network traffic snapshot covering all three of the channel traffic
     * monitors. This snapshot function will not use synchronization during creation,
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
     * Freezes any transient traffic monitoring for this monitor, across all three of the
     * channel monitors. The freeze function does not stop active connection tracking,
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
