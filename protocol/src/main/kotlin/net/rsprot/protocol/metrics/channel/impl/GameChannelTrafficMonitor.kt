package net.rsprot.protocol.metrics.channel.impl

import net.rsprot.protocol.metrics.channel.ChannelTrafficMonitor

/**
 * A wrapper for game channel traffic monitor.
 * The wrapper class allows us to write extension functions specifically targeted
 * at the game-implementation.
 * @property channelTrafficMonitor the underlying traffic monitor to which calls are delegated.
 */
public class GameChannelTrafficMonitor(
    private val channelTrafficMonitor: ChannelTrafficMonitor,
) : ChannelTrafficMonitor by channelTrafficMonitor
